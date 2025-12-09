package pro.sky.bank.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "bank-recommendation-service");
        response.put("status", "running");
        response.put("timestamp", System.currentTimeMillis());

        try {
            // Проверяем подключение к БД
            Integer test = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", Map.of(
                    "status", "connected",
                    "testResult", test
            ));

            // Проверяем таблицы
            String[] tables = {"USERS", "PRODUCTS", "TRANSACTIONS"};
            Map<String, Boolean> tablesStatus = new HashMap<>();
            for (String table : tables) {
                try {
                    Boolean exists = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) > 0 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                            Boolean.class,
                            table
                    );
                    tablesStatus.put(table, exists != null && exists);
                } catch (Exception e) {
                    tablesStatus.put(table, false);
                }
            }
            response.put("tables", tablesStatus);

        } catch (Exception e) {
            response.put("database", Map.of(
                    "status", "disconnected",
                    "error", e.getMessage()
            ));
        }

        return response;
    }
}
