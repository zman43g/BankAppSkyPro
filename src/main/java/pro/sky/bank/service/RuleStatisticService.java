package pro.sky.bank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.bank.model.entity.RuleStatistic;
import pro.sky.bank.repository.RuleStatisticRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RuleStatisticService {

    private final RuleStatisticRepository statisticRepository;
    private final DynamicRuleService dynamicRuleService;

    public RuleStatisticService(RuleStatisticRepository statisticRepository,
                                DynamicRuleService dynamicRuleService) {
        this.statisticRepository = statisticRepository;
        this.dynamicRuleService = dynamicRuleService;
    }


    public void incrementTrigger(String productId, String productName) {
        // Создаем или обновляем запись статистики
        RuleStatistic statistic = statisticRepository.findByProductId(productId)
                .orElseGet(() -> {
                    RuleStatistic newStat = new RuleStatistic();
                    newStat.setProductId(productId);
                    newStat.setProductName(productName);
                    newStat.setTriggerCount(0L);
                    newStat.setIsActive(true);
                    return statisticRepository.save(newStat);
                });

        if (!statistic.getProductName().equals(productName)) {
            statistic.setProductName(productName);
        }

        statisticRepository.incrementTriggerCount(productId, LocalDateTime.now());
    }


    public void deactivateStatistic(String productId) {
        statisticRepository.deactivateByProductId(productId);
    }

    public Map<String, Object> getFullStatistics() {
        Map<String, Object> stats = new HashMap<>();

        var allRules = dynamicRuleService.getAllRules();

        List<RuleStatistic> allStatistics = statisticRepository.findAll();

        Map<String, RuleStatistic> statsMap = new HashMap<>();
        allStatistics.forEach(stat -> statsMap.put(stat.getProductId(), stat));

        List<Map<String, Object>> ruleStats = allRules.stream()
                .map(rule -> {
                    Map<String, Object> ruleStat = new HashMap<>();
                    ruleStat.put("productId", rule.getProductId());
                    ruleStat.put("productName", rule.getProductName());

                    RuleStatistic stat = statsMap.get(rule.getProductId());
                    if (stat != null) {
                        ruleStat.put("triggerCount", stat.getTriggerCount());
                        ruleStat.put("lastTriggered", stat.getLastTriggered());
                        ruleStat.put("isActive", stat.getIsActive());
                    } else {
                        ruleStat.put("triggerCount", 0);
                        ruleStat.put("lastTriggered", null);
                        ruleStat.put("isActive", true);
                    }

                    return ruleStat;
                })
                .toList();

        long totalTriggers = allStatistics.stream()
                .mapToLong(RuleStatistic::getTriggerCount)
                .sum();

        long activeRules = allRules.size();
        long activeStats = statisticRepository.countByIsActiveTrue();

        stats.put("totalRules", allRules.size());
        stats.put("activeRules", activeRules);
        stats.put("rulesWithStatistics", allStatistics.size());
        stats.put("activeStatistics", activeStats);
        stats.put("totalTriggerCount", totalTriggers);
        stats.put("averageTriggersPerRule", allRules.isEmpty() ? 0 : (double) totalTriggers / allRules.size());
        stats.put("ruleStatistics", ruleStats);
        stats.put("timestamp", LocalDateTime.now());

        List<Map<String, Object>> topRules = statisticRepository.findAllOrderByTriggerCountDesc()
                .stream()
                .limit(10)
                .map(stat -> {
                    Map<String, Object> top = new HashMap<>();
                    top.put("productId", stat.getProductId());
                    top.put("productName", stat.getProductName());
                    top.put("triggerCount", stat.getTriggerCount());
                    top.put("lastTriggered", stat.getLastTriggered());
                    return top;
                })
                .toList();

        stats.put("topRules", topRules);

        return stats;
    }

    public Map<String, Object> getStatisticByProductId(String productId) {
        Map<String, Object> result = new HashMap<>();

        boolean ruleExists = dynamicRuleService.ruleExists(productId);
        result.put("ruleExists", ruleExists);

        if (!ruleExists) {
            result.put("message", "Правило не найдено");
            return result;
        }

        RuleStatistic statistic = statisticRepository.findByProductId(productId)
                .orElse(null);

        if (statistic != null) {
            result.put("productId", statistic.getProductId());
            result.put("productName", statistic.getProductName());
            result.put("triggerCount", statistic.getTriggerCount());
            result.put("lastTriggered", statistic.getLastTriggered());
            result.put("createdAt", statistic.getCreatedAt());
            result.put("isActive", statistic.getIsActive());
        } else {
            // Правило есть, но статистики нет = 0 срабатываний
            result.put("productId", productId);
            result.put("triggerCount", 0);
            result.put("isActive", true);
            result.put("message", "Статистика отсутствует (0 срабатываний)");
        }

        return result;
    }
}
