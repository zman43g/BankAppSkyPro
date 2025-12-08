package pro.sky.bank.rules;

import pro.sky.bank.model.Recommendation;
import pro.sky.bank.repository.RecommendationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleCreditRule implements RecommendationRule {

    private final RecommendationsRepository repository;

    @Autowired
    public SimpleCreditRule(RecommendationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Recommendation> getRecommendation(UUID userId) {
        boolean hasCredit = repository.hasProductType(userId, "CREDIT");
        BigDecimal debitDeposits = repository.getTotalDepositsByProductType(userId, "DEBIT");
        BigDecimal debitExpenses = repository.getTotalExpensesByProductType(userId, "DEBIT");

        boolean rule1 = !hasCredit;
        boolean rule2 = debitDeposits.compareTo(debitExpenses) > 0;
        boolean rule3 = debitExpenses.compareTo(new BigDecimal("100000")) > 0;

        if (rule1 && rule2 && rule3) {
            Recommendation recommendation = new Recommendation(
                    "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
                    "Простой кредит",
                    "Позвольте себе больше!"
            );
            return Optional.of(recommendation);
        }

        return Optional.empty();
    }
}