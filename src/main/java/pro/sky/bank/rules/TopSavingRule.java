package pro.sky.bank.rules;

import pro.sky.bank.model.Recommendation;
import pro.sky.bank.repository.RecommendationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class TopSavingRule implements RecommendationRule {

    private final RecommendationsRepository repository;

    @Autowired
    public TopSavingRule(RecommendationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Recommendation> getRecommendation(UUID userId) {
        boolean hasDebit = repository.hasProductType(userId, "DEBIT");
        BigDecimal debitDeposits = repository.getTotalDepositsByProductType(userId, "DEBIT");
        BigDecimal savingDeposits = repository.getTotalDepositsByProductType(userId, "SAVING");
        BigDecimal debitExpenses = repository.getTotalExpensesByProductType(userId, "DEBIT");

        boolean rule1 = hasDebit;
        boolean rule2 = debitDeposits.compareTo(new BigDecimal("50000")) >= 0 ||
                savingDeposits.compareTo(new BigDecimal("50000")) >= 0;
        boolean rule3 = debitDeposits.compareTo(debitExpenses) > 0;

        if (rule1 && rule2 && rule3) {
            Recommendation recommendation = new Recommendation(
                    "59efc529-2fff-41af-baff-90ccd7402925",
                    "Top Saving",
                    "Копилка."
            );
            return Optional.of(recommendation);
        }

        return Optional.empty();
    }
}
