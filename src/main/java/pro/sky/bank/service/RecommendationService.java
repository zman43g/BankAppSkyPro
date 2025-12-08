package pro.sky.bank.service;

import pro.sky.bank.model.Recommendation;
import pro.sky.bank.model.RecommendationResponse;
import pro.sky.bank.rules.RecommendationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final List<RecommendationRule> rules;

    @Autowired
    public RecommendationService(List<RecommendationRule> rules) {
        this.rules = rules;
    }

    public RecommendationResponse getRecommendations(UUID userId) {
        List<Recommendation> recommendations = rules.stream()
                .map(rule -> rule.getRecommendation(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new RecommendationResponse(userId.toString(), recommendations);
    }
}