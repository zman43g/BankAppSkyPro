package pro.sky.bank.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DatabaseChecker {

    private static final Logger log = LoggerFactory.getLogger(DatabaseChecker.class);

    @Bean
    public CommandLineRunner checkDatabaseConnection(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("=== TESTING DATABASE CONNECTION ===");

            try {

                try (Connection connection = dataSource.getConnection()) {
                    String url = connection.getMetaData().getURL();
                    String driver = connection.getMetaData().getDriverName();
                    log.info("✅ Подключение к базе данных установлено");
                    log.info("   URL: {}", url);
                    log.info("   Драйвер: {}", driver);

                    checkTables(jdbcTemplate);
                    checkTestUsers(jdbcTemplate);

                }
            } catch (Exception e) {
                log.error("❌ Ошибка подключения к базе данных: {}", e.getMessage());
                log.error("Проверьте:");
                log.error("1. Наличие файла transaction.mv.db");
                log.error("2. Правильность пути в spring.datasource.url");
                log.error("3. Что файл не поврежден");
                e.printStackTrace();
            }
        };
    }

    private void checkTables(JdbcTemplate jdbcTemplate) {
        log.info("\n=== CHECKING TABLES ===");

        String[] requiredTables = {"USERS", "PRODUCTS", "TRANSACTIONS"};
        for (String table : requiredTables) {
            try {
                Boolean exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) > 0 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                        Boolean.class,
                        table
                );
                log.info("   {}: {}", table, exists != null && exists ? "✅ найдена" : "❌ не найдена");

                if (exists != null && exists) {
                    // Показываем количество записей
                    Long count = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM " + table,
                            Long.class
                    );
                    log.info("     Записей: {}", count);
                }
            } catch (Exception e) {
                log.error("   {}: ❌ ошибка проверки: {}", table, e.getMessage());
            }
        }
    }

    private void checkTestUsers(JdbcTemplate jdbcTemplate) {
        log.info("\n=== TEST USERS ===");

        String[] testUserIds = {
                "cd515076-5d8a-44be-930e-8d4fcb79f42d",
                "d4a4d619-9a0c-4fc5-b0cb-76c49409546b",
                "fa9fd2e9-2c06-4336-a32e-bdec6e0c9cfb"
        };

        for (String userId : testUserIds) {
            try {
                Boolean exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) > 0 FROM USERS WHERE id = ?",
                        Boolean.class,
                        userId
                );
                log.info("   {}: {}", userId, exists != null && exists ? "✅ существует" : "❌ не найден");
            } catch (Exception e) {
                log.error("   {}: ❌ ошибка проверки: {}", userId, e.getMessage());
            }
        }
    }
}