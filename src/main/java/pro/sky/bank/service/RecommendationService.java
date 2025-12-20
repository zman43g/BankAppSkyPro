package pro.sky.bank.service;

import pro.sky.bank.model.dto.DynamicRuleResponse;
import pro.sky.bank.model.Recommendation;
import pro.sky.bank.model.RecommendationResponse;
import pro.sky.bank.model.dto.RuleQuery;
import pro.sky.bank.rules.RecommendationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Autowired
    private final List<RecommendationRule> staticRules;
    private final DynamicRuleService dynamicRuleService;
    private final RuleEvaluationService ruleEvaluationService;

    /**
     * Генерирует персонализированный список рекомендаций для пользователя.
     * Алгоритм формирует итоговый список, объединяя результаты оценки двух типов правил:
     *   Статические правила: На основе заранее определенных классов, реализующих {@link RecommendationRule}.
     *   Динамические правила:На основе правил, управляемых через {@link DynamicRuleService} и оцениваемых {@link RuleEvaluationService}.
     * Каждое правило, условие которого выполняется для данного пользователя, порождает одну рекомендацию.
     *
     * @param userId Уникальный идентификатор пользователя ({@link UUID}), для которого запрашиваются рекомендации.
     * @return {@link RecommendationResponse}, содержащий идентификатор пользователя и список объектов {@link Recommendation}.
     */
    public RecommendationResponse getRecommendations(UUID userId) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Добавляем статические рекомендации
        recommendations.addAll(getStaticRecommendations(userId));

        // Добавляем динамические рекомендации
        recommendations.addAll(getDynamicRecommendations(userId));

        System.out.println("Found " + recommendations.size() + " recommendations for user: " + userId);

        // Создаем и возвращаем RecommendationResponse
        return new RecommendationResponse(userId.toString(), recommendations);
    }

    private List<Recommendation> getStaticRecommendations(UUID userId) {
        if (staticRules == null || staticRules.isEmpty()) {
            System.out.println("No static rules found");
            return new ArrayList<>();
        }

        return staticRules.stream()
                .map(rule -> rule.getRecommendation(userId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }

    private List<Recommendation> getDynamicRecommendations(UUID userId) {
        List<Recommendation> recommendations = new ArrayList<>();

        try {
            // Получаем все динамические правила
            List<DynamicRuleResponse> dynamicRules = dynamicRuleService.getAllRules();

            for (DynamicRuleResponse rule : dynamicRules) {
                if (evaluateDynamicRule(userId, rule)) {
                    Recommendation recommendation = new Recommendation(
                            rule.getProductId(),
                            rule.getProductName(),
                            rule.getProductText()
                    );
                    recommendations.add(recommendation);
                    System.out.println("Dynamic rule matched for user: " + userId + ", product: " + rule.getProductName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting dynamic recommendations: " + e.getMessage());
        }

        return recommendations;
    }

    private boolean evaluateDynamicRule(UUID userId, DynamicRuleResponse rule) {
        // Правило выполняется, если все его запросы возвращают true
        for (RuleQuery query : rule.getRule()) {
            if (!ruleEvaluationService.evaluateQuery(userId, query)) {
                return false;
            }
        }
        return true;
    }
}