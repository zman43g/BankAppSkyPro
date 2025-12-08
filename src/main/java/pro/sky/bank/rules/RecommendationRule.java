package pro.sky.bank.rules;

import pro.sky.bank.model.Recommendation;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationRule {
    Optional<Recommendation> getRecommendation(UUID userId);
}