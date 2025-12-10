package pro.sky.bank.repository;

import pro.sky.bank.model.entity.RuleQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RuleQueryRepository extends JpaRepository<RuleQueryEntity, UUID> {
}