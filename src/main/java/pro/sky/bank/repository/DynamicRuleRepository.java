package pro.sky.bank.repository;

import pro.sky.bank.model.entity.DynamicRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DynamicRuleRepository extends JpaRepository<DynamicRule, Long> {

    boolean existsByProductId(String productId);

    void deleteByProductId(String productId);
    Optional<DynamicRule> findByProductId(String productId);
}