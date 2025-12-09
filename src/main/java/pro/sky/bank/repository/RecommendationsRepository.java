package pro.sky.bank.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class RecommendationsRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(RecommendationsRepository.class);

    public RecommendationsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getRandomTransactionAmount(UUID user) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT amount FROM transactions t WHERE t.user_id = ? LIMIT 1",
                Integer.class,
                user.toString());
        return result != null ? result : 0;
    }

    public boolean hasProductType(UUID userId, String productType) {
        String sql = """
            SELECT COUNT(*) > 0 
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.user_id = ? AND p.type = ?
            """;
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString(), productType);
        return result != null && result;
    }

    public BigDecimal getTotalDepositsByProductType(UUID userId, String productType) {
        String sql = """
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.user_id = ? 
              AND p.type = ? 
              AND t.type = 'DEPOSIT'
            """;
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString(), productType);
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpensesByProductType(UUID userId, String productType) {
        String sql = """
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.user_id = ? 
              AND p.type = ? 
              AND t.type = 'EXPENSE'
            """;
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString(), productType);
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getTotalAmountByProductTypeAndTransactionType(UUID userId, String productType, String transactionType) {
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
    }

    public boolean hasProduct(UUID userId, UUID productId) {
        String sql = """
            SELECT COUNT(*) > 0 
            FROM transactions t
            WHERE t.user_id = ? AND t.product_id = ?
            """;
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class,
                userId.toString(), productId.toString());
        return result != null && result;
    }

    public BigDecimal getTotalDeposits(UUID userId) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0)
            FROM transactions
            WHERE user_id = ? AND type = 'DEPOSIT'
            """;
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString());
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpenses(UUID userId) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0)
            FROM transactions
            WHERE user_id = ? AND type = 'EXPENSE'
            """;
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString());
        return result != null ? result : BigDecimal.ZERO;
    }

    public int getDistinctProductCount(UUID userId) {
        String sql = """
            SELECT COUNT(DISTINCT product_id)
            FROM transactions
            WHERE user_id = ?
            """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId.toString());
        return result != null ? result : 0;
    }

    public List<UUID> getUserProductIds(UUID userId) {
        String sql = """
            SELECT DISTINCT product_id
            FROM transactions
            WHERE user_id = ?
            """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> UUID.fromString(rs.getString("product_id")),
                userId.toString());
    }

    public String getProductType(UUID productId) {
        String sql = "SELECT type FROM products WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, productId.toString());
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("❌ Тест подключения к БД: ошибка - {}", e.getMessage());
        }
    }
}