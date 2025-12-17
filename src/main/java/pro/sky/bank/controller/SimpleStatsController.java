package pro.sky.bank.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import pro.sky.bank.service.RuleStatisticService;

import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SimpleStatsController {

    private final RuleStatisticService statisticService;
    private final Instant startTime = Instant.now();

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    public SimpleStatsController(RuleStatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/rule/stats")
    public Map<String, Object> getRuleStats() {
        return statisticService.getFullStatistics();
    }

    @GetMapping("/rule/stats/{productId}")
    public Map<String, Object> getRuleStat(@PathVariable String productId) {
        return statisticService.getStatisticByProductId(productId);
    }

    @PostMapping("/management/clear-caches")
    public Map<String, String> clearCaches() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Кэши очищены (заглушка)");
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @GetMapping("/management/info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("name", appName);
        info.put("version", appVersion);

        Duration uptime = Duration.between(startTime, Instant.now());
        info.put("uptime", formatUptime(uptime));
        info.put("startTime", startTime.toString());

        return info;
    }

    private String formatUptime(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}