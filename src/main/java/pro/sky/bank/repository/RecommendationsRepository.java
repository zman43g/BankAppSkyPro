package pro.sky.bank.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RecommendationsRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(RecommendationsRepository.class);

    // Кэши для запросов
    private final Cache<String, Boolean> userOfCache;
    private final Cache<String, Boolean> activeUserOfCache;
    private final Cache<String, BigDecimal> transactionSumCache;
    private final Cache<String, Integer> transactionCountCache;

    public RecommendationsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Инициализация кэшей
        this.userOfCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.activeUserOfCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.transactionSumCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.transactionCountCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("RecommendationsRepository инициализирован с кэшированием");
        testConnection();
    }

    public int getRandomTransactionAmount(UUID user) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT amount FROM transactions t WHERE t.user_id = ? LIMIT 1",
                Integer.class,
                user.toString());
        return result != null ? result : 0;
    }

    public boolean hasProductType(UUID userId, String productType) {
        String cacheKey = userId.toString() + ":" + productType;

        return userOfCache.get(cacheKey, key -> {
            try {
                String sql = """
                    SELECT COUNT(*) > 0 
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? AND p.type = ?
                    """;
                Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString(), productType);
                return result != null && result;
            } catch (Exception e) {
                log.error("Ошибка в hasProductType: {}", e.getMessage());
                return false;
            }
        });
    }

    // Новый метод для ACTIVE_USER_OF запроса (5+ транзакций)
    public boolean isActiveUserOfProductType(UUID userId, String productType) {
        String cacheKey = userId.toString() + ":" + productType + ":active";

        return activeUserOfCache.get(cacheKey, key -> {
            try {
                String sql = """
                    SELECT COUNT(*) >= 5
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? AND p.type = ?
                    """;
                Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString(), productType);
                return result != null && result;
            } catch (Exception e) {
                log.error("Ошибка в isActiveUserOfProductType: {}", e.getMessage());
                return false;
            }
        });
    }

    // Новый метод для получения количества транзакций по типу продукта
    public int getTransactionCountByProductType(UUID userId, String productType) {
        String cacheKey = userId.toString() + ":" + productType + ":count";

        return transactionCountCache.get(cacheKey, key -> {
            try {
                String sql = """
                    SELECT COUNT(*)
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? AND p.type = ?
                    """;
                Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId.toString(), productType);
                return result != null ? result : 0;
            } catch (Exception e) {
                log.error("Ошибка в getTransactionCountByProductType: {}", e.getMessage());
                return 0;
            }
        });
    }

    public BigDecimal getTotalDepositsByProductType(UUID userId, String productType) {
        return getTransactionSumByProductTypeAndTransactionType(userId, productType, "DEPOSIT");
    }

    public BigDecimal getTotalExpensesByProductType(UUID userId, String productType) {
        return getTransactionSumByProductTypeAndTransactionType(userId, productType, "EXPENSE");
    }

    // Обновленный метод с кэшированием
    public BigDecimal getTransactionSumByProductTypeAndTransactionType(
            UUID userId, String productType, String transactionType) {
        String cacheKey = userId.toString() + ":" + productType + ":" + transactionType;

        return transactionSumCache.get(cacheKey, key -> {
            try {
                String sql = """
                    SELECT COALESCE(SUM(t.amount), 0)
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? 
                      AND p.type = ? 
                      AND t.type = ?
                    """;
                BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class,
                        userId.toString(), productType, transactionType);
                return result != null ? result : BigDecimal.ZERO;
            } catch (Exception e) {
                log.error("Ошибка в getTransactionSumByProductTypeAndTransactionType: {}", e.getMessage());
                return BigDecimal.ZERO;
            }
        });
    }

    public BigDecimal getTotalAmountByProductTypeAndTransactionType(UUID userId, String productType, String transactionType) {
        // Используем кэшированную версию
        return getTransactionSumByProductTypeAndTransactionType(userId, productType, transactionType);
    }

    public boolean hasProduct(UUID userId, UUID productId) {
        try {
            String sql = """
                SELECT COUNT(*) > 0 
                FROM transactions t
                WHERE t.user_id = ? AND t.product_id = ?
                """;
            Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class,
                    userId.toString(), productId.toString());
            return result != null && result;
        } catch (Exception e) {
            log.error("Ошибка в hasProduct: {}", e.getMessage());
            return false;
        }
    }

    public BigDecimal getTotalDeposits(UUID userId) {
        try {
            String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE user_id = ? AND type = 'DEPOSIT'
                """;
            BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString());
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Ошибка в getTotalDeposits: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalExpenses(UUID userId) {
        try {
            String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE user_id = ? AND type = 'EXPENSE'
                """;
            BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString());
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Ошибка в getTotalExpenses: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public int getDistinctProductCount(UUID userId) {
        try {
            String sql = """
                SELECT COUNT(DISTINCT product_id)
                FROM transactions
                WHERE user_id = ?
                """;
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId.toString());
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Ошибка в getDistinctProductCount: {}", e.getMessage());
            return 0;
        }
    }

    public List<UUID> getUserProductIds(UUID userId) {
        try {
            String sql = """
                SELECT DISTINCT product_id
                FROM transactions
                WHERE user_id = ?
                """;
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> UUID.fromString(rs.getString("product_id")),
                    userId.toString());
        } catch (Exception e) {
            log.error("Ошибка в getUserProductIds: {}", e.getMessage());
            return List.of();
        }
    }

    public String getProductType(UUID productId) {
        try {
            String sql = "SELECT type FROM products WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, productId.toString());
        } catch (Exception e) {
            log.error("Ошибка в getProductType: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTotalDepositsExceeds(UUID userId, String productType, BigDecimal threshold) {
        BigDecimal total = getTotalDepositsByProductType(userId, productType);
        return total.compareTo(threshold) > 0;
    }

    public boolean isTotalExpensesExceeds(UUID userId, String productType, BigDecimal threshold) {
        BigDecimal total = getTotalExpensesByProductType(userId, productType);
        return total.compareTo(threshold) > 0;
    }

    public boolean isDepositsGreaterThanExpenses(UUID userId, String productType) {
        BigDecimal deposits = getTotalDepositsByProductType(userId, productType);
        BigDecimal expenses = getTotalExpensesByProductType(userId, productType);
        return deposits.compareTo(expenses) > 0;
    }

    // Метод для проверки подключения
    public void testConnection() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("✅ Тест подключения к БД: успешно (результат: {})", result);

            // Проверяем таблицы
            checkTables();

        } catch (Exception e) {
            log.error("❌ Тест подключения к БД: ошибка - {}", e.getMessage());
        }
    }

    private void checkTables() {
        String[] tables = {"users", "products", "transactions"};
        for (String table : tables) {
            try {
                Long count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + table, Long.class);
                log.info("📊 Таблица {}: {} записей", table, count);
            } catch (Exception e) {
                log.warn("⚠️ Таблица {}: не найдена - {}", table, e.getMessage());
            }
        }
    }

    // Метод для сброса кэша для конкретного пользователя
    public void clearCacheForUser(UUID userId) {
        String userIdStr = userId.toString();

        userOfCache.asMap().keySet().removeIf(key -> key.startsWith(userIdStr + ":"));
        activeUserOfCache.asMap().keySet().removeIf(key -> key.startsWith(userIdStr + ":"));
        transactionSumCache.asMap().keySet().removeIf(key -> key.startsWith(userIdStr + ":"));
        transactionCountCache.asMap().keySet().removeIf(key -> key.startsWith(userIdStr + ":"));

        log.debug("Кэш очищен для пользователя: {}", userId);
    }

    // Метод для получения статистики кэша
    public String getCacheStats() {
        return String.format(
                "UserOfCache: hits=%s, misses=%s, size=%s | " +
                        "ActiveUserCache: hits=%s, misses=%s, size=%s | " +
                        "TransactionSumCache: hits=%s, misses=%s, size=%s | " +
                        "TransactionCountCache: hits=%s, misses=%s, size=%s",
                userOfCache.stats().hitCount(),
                userOfCache.stats().missCount(),
                userOfCache.estimatedSize(),
                activeUserOfCache.stats().hitCount(),
                activeUserOfCache.stats().missCount(),
                activeUserOfCache.estimatedSize(),
                transactionSumCache.stats().hitCount(),
                transactionSumCache.stats().missCount(),
                transactionSumCache.estimatedSize(),
                transactionCountCache.stats().hitCount(),
                transactionCountCache.stats().missCount(),
                transactionCountCache.estimatedSize()
        );
    }

    // Метод для очистки кэша
    public void clearAllCaches() {
        userOfCache.invalidateAll();
        activeUserOfCache.invalidateAll();
        transactionSumCache.invalidateAll();
        transactionCountCache.invalidateAll();
        log.info("Все кэши очищены");
    }
}