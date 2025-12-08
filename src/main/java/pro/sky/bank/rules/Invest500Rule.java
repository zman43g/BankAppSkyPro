package pro.sky.bank.rules;

import pro.sky.bank.model.Recommendation;
import pro.sky.bank.repository.RecommendationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class Invest500Rule implements RecommendationRule {

    private final RecommendationsRepository repository;

    @Autowired
    public Invest500Rule(RecommendationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Recommendation> getRecommendation(UUID userId) {
        boolean hasDebit = repository.hasProductType(userId, "DEBIT");
        boolean hasInvest = repository.hasProductType(userId, "INVEST");
        BigDecimal savingDeposits = repository.getTotalDepositsByProductType(userId, "SAVING");

        boolean rule1 = hasDebit;
        boolean rule2 = !hasInvest;
        boolean rule3 = savingDeposits.compareTo(new BigDecimal("1000")) > 0;

        if (rule1 && rule2 && rule3) {
            Recommendation recommendation = new Recommendation("147f6a0f-3b91-413b-ab99-87f081d60d5a",
                    "Invest 500",
                    "Индивидуальный инвестиционным счет"
            );
            return Optional.of(recommendation);
        }

        return Optional.empty();
    }
}
