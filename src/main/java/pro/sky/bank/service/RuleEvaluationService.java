package pro.sky.bank.service;

import pro.sky.bank.model.dto.RuleQuery;
import pro.sky.bank.model.dto.DynamicRuleResponse;
import pro.sky.bank.repository.RecommendationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {

    private final RecommendationsRepository repository;
    private final RuleStatisticService statisticService;
    private final DynamicRuleService dynamicRuleService;
    /**
     * Выполняет оценку одного запроса ({@link RuleQuery}) в контексте конкретного пользователя.
     * Это ключевой метод для проверки бизнес-условий. Помимо вычисления результата, метод:
     *   Учитывает флаг {@code negate} в запросе для инверсии результата.
     *   Собирает статистику срабатывания через {@link RuleStatisticService}.
     *   Обеспечивает логирование и обработку ошибок.
     * Метод выполняется в транзакции.
     *
     * @param userId Уникальный идентификатор пользователя ({@link UUID}), для которого оценивается запрос.
     * @param ruleQuery Объект {@link RuleQuery}, содержащий тип, аргументы и флаг отрицания оцениваемого условия.
     * @return {@code true} если условие запроса выполняется (с учетом флага {@code negate}),
     *         {@code false} в противном случае или в случае ошибки.
     */
    @Transactional
    public boolean evaluateQuery(UUID userId, RuleQuery ruleQuery) {

        try {
            boolean result = evaluateQueryInternal(userId, ruleQuery);

            // Применяем отрицание если нужно
            if (Boolean.TRUE.equals(ruleQuery.getNegate())) {
                result = !result;
            }

            collectStatistics(ruleQuery, result);

            log.info("Query evaluated: userId={}, query={}, result={}", userId, ruleQuery.getQuery(), result);
            return result;

        } catch (Exception e) {
            log.error("Error evaluating query: {}", e.getMessage(), e);
            return false;
        }
    }


    private void collectStatistics(RuleQuery ruleQuery, boolean evaluationResult) {
        try {
            Long ruleId = ruleQuery.getRuleId();
            String productId = ruleQuery.getProductId();
            String productName = ruleQuery.getProductName();

            if (productId != null && productName != null) {
                // Увеличиваем счетчик срабатываний
                statisticService.incrementTrigger(productId, productName);

                log.debug("Statistics collected for rule: {}, productId: {}, result: {}",
                        ruleId, productId, evaluationResult);
            }
        } catch (Exception e) {
            log.warn("Failed to collect statistics: {}", e.getMessage());
        }
    }

    private boolean evaluateQueryInternal(UUID userId, RuleQuery ruleQuery) {
        RuleQuery.QueryType queryType = RuleQuery.QueryType.valueOf(ruleQuery.getQuery());
        var arguments = ruleQuery.getArguments();

        switch (queryType) {
            case USER_OF:
                return evaluateUserOf(userId, arguments.get(0));

            case ACTIVE_USER_OF:
                return evaluateActiveUserOf(userId, arguments.get(0));

            case TRANSACTION_SUM_COMPARE:
                return evaluateTransactionSumCompare(userId, arguments);

            case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW:
                return evaluateTransactionSumCompareDepositWithdraw(userId, arguments);

            default:
                throw new IllegalArgumentException("Unknown query type: " + queryType);
        }
    }

    private boolean evaluateUserOf(UUID userId, String productType) {
        return repository.hasProductType(userId, productType);
    }

    private boolean evaluateActiveUserOf(UUID userId, String productType) {
        return repository.isActiveUserOfProductType(userId, productType);
    }

    private boolean evaluateTransactionSumCompare(UUID userId, java.util.List<String> arguments) {
        String productType = arguments.get(0);
        String transactionType = arguments.get(1);
        String operator = arguments.get(2);
        BigDecimal threshold = new BigDecimal(arguments.get(3));

        BigDecimal sum = repository.getTransactionSumByProductTypeAndTransactionType(
                userId, productType, transactionType);

        return compareValues(sum, threshold, operator);
    }

    private boolean evaluateTransactionSumCompareDepositWithdraw(UUID userId, java.util.List<String> arguments) {
        String productType = arguments.get(0);
        String operator = arguments.get(1);

        BigDecimal deposits = repository.getTotalDepositsByProductType(userId, productType);
        BigDecimal expenses = repository.getTotalExpensesByProductType(userId, productType);

        return compareValues(deposits, expenses, operator);
    }

    private boolean compareValues(BigDecimal value1, BigDecimal value2, String operator) {
        RuleQuery.ComparisonOperator op = RuleQuery.ComparisonOperator.fromSymbol(operator);

        switch (op) {
            case GREATER:
                return value1.compareTo(value2) > 0;
            case LESS:
                return value1.compareTo(value2) < 0;
            case EQUAL:
                return value1.compareTo(value2) == 0;
            case GREATER_EQUAL:
                return value1.compareTo(value2) >= 0;
            case LESS_EQUAL:
                return value1.compareTo(value2) <= 0;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

       @Transactional
    public boolean evaluateFullRule(UUID userId, DynamicRuleResponse rule) {
        try {
            boolean finalResult = true; // Начальное значение для AND логики

            if (rule.getRule() != null && !rule.getRule().isEmpty()) {
                for (RuleQuery query : rule.getRule()) {
                    // Устанавливаем ruleId для каждого запроса
                    query.setRuleId(rule.getId());

                    boolean queryResult = evaluateQuery(userId, query);

                    // Предполагаем AND логику между запросами
                    finalResult = finalResult && queryResult;

                    // Если уже false, можно прервать выполнение
                    if (!finalResult) {
                        break;
                    }
                }
            }

            // Собираем финальную статистику для всего правила
            statisticService.incrementTrigger(rule.getProductId(), rule.getProductName());

            log.info("Full rule evaluated: ruleId={}, productId={}, result={}",
                    rule.getId(), rule.getProductId(), finalResult);

            return finalResult;

        } catch (Exception e) {
            log.error("Error evaluating full rule: {}", e.getMessage(), e);
            return false;
        }
    }


    @Transactional
    public java.util.List<DynamicRuleResponse> evaluateRulesForUser(UUID userId) {
        try {
            // Получаем все правила
            var allRules = dynamicRuleService.getAllRules();

            // Оцениваем каждое правило
            var applicableRules = allRules.stream()
                    .filter(rule -> evaluateFullRule(userId, rule))
                    .toList();

            log.info("Rules evaluated for user: userId={}, totalRules={}, applicableRules={}",
                    userId, allRules.size(), applicableRules.size());

            return applicableRules;

        } catch (Exception e) {
            log.error("Error evaluating rules for user: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }
}