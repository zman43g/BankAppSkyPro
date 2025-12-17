package pro.sky.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.bank.model.entity.RuleStatistic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RuleStatisticRepository extends JpaRepository<RuleStatistic, Long> {

    Optional<RuleStatistic> findByProductId(String productId);

    List<RuleStatistic> findByIsActiveTrue();

    List<RuleStatistic> findByIsActiveFalse();

    @Modifying
    @Query("UPDATE RuleStatistic rs SET rs.isActive = false WHERE rs.productId = :productId")
    void deactivateByProductId(@Param("productId") String productId);

    @Modifying
    @Query("UPDATE RuleStatistic rs SET rs.triggerCount = rs.triggerCount + 1, " +
            "rs.lastTriggered = :triggerTime, rs.updatedAt = :triggerTime " +
            "WHERE rs.productId = :productId")
    void incrementTriggerCount(@Param("productId") String productId,
                               @Param("triggerTime") LocalDateTime triggerTime);

    @Query("SELECT rs FROM RuleStatistic rs ORDER BY rs.triggerCount DESC")
    List<RuleStatistic> findAllOrderByTriggerCountDesc();

    long countByIsActiveTrue();
}