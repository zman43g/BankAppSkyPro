package pro.sky.bank.service;

import pro.sky.bank.model.dto.RuleQuery;
import pro.sky.bank.repository.RecommendationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {

    private final RecommendationsRepository repository;

    public boolean evaluateQuery(UUID userId, RuleQuery ruleQuery) {

        try {
            boolean result = evaluateQueryInternal(userId, ruleQuery);

            // Применяем отрицание если нужно
            if (Boolean.TRUE.equals(ruleQuery.getNegate())) {
                result = !result;
            }

            System.out.println("Query evaluated: userId=" + userId + ", query=" + ruleQuery.getQuery() + ", result=" + result);
            return result;

        } catch (Exception e) {
            System.err.println("Error evaluating query: " + e.getMessage());
            return false;
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
}