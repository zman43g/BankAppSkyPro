package pro.sky.bank.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SimpleStatsController {

    private final Map<String, Integer> ruleStats = new HashMap<>();
    private long startTime = System.currentTimeMillis();

    // Увеличиваем счетчик срабатываний правила
    public void incrementRuleTrigger(String productId) {
        ruleStats.put(productId, ruleStats.getOrDefault(productId, 0) + 1);
    }

    @GetMapping("/rule/stats")
    public Map<String, Object> getRuleStats() {
        Map<String, Object> stats = new HashMap<>();

        // Общая статистика
        int totalTriggers = ruleStats.values().stream().mapToInt(Integer::intValue).sum();
        stats.put("totalRulesTriggered", ruleStats.size());
        stats.put("totalTriggerCount", totalTriggers);

        // Статистика по правилам
        stats.put("ruleTriggers", ruleStats);

        // Топ правил
        Map<String, Integer> topRules = new HashMap<>();
        ruleStats.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(entry -> topRules.put(entry.getKey(), entry.getValue()));

        stats.put("topRules", topRules);
        stats.put("timestamp", System.currentTimeMillis());

        return stats;
    }

    @PostMapping("/management/clear-caches")
    public Map<String, String> clearCaches() {
        // В реальной системе здесь очистка Spring Cache
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Кэши очищены (заглушка)");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    @GetMapping("/management/info")
    public Map<String, String> getSystemInfo() {
        Map<String, String> info = new HashMap<>();

        long uptimeMs = System.currentTimeMillis() - startTime;
        long uptimeSeconds = uptimeMs / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;

        info.put("name", "Bank Dynamic Rules System");
        info.put("version", "1.0.0");
        info.put("description", "Система динамических правил и рекомендаций");
        info.put("environment", "production");
        info.put("uptime", String.format("%dч %dм %dс", hours, minutes, seconds));
        info.put("startTime", String.valueOf(startTime));

        return info;
    }
}