package pro.sky.bank.repository;

import pro.sky.bank.model.entity.DynamicRule;
import pro.sky.bank.model.entity.RuleQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RuleQueryRepository extends JpaRepository<RuleQueryEntity, UUID> {
    void deleteByRule(DynamicRule rule);
}