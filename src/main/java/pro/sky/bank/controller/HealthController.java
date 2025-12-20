package pro.sky.bank.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для проверки работоспособности (health check) приложения.
 * <p>
 * Предоставляет эндпоинт для мониторинга состояния системы, который может использоваться
 * оркестраторами контейнеров (Kubernetes, Docker Swarm), системами мониторинга
 * или администраторами для проверки доступности сервиса.
 * </p>
 * <p>
 * Проверка включает:
 * <ul>
 *   <li>Общий статус сервиса</li>
 *   <li>Проверку подключения к базе данных</li>
 *   <li>Проверку наличия ключевых таблиц в базе данных</li>
 * </ul>
 * </p>
 *
 * @see JdbcTemplate
 * @see GetMapping
 * @see RestController
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Конструктор для внедрения зависимости JdbcTemplate.
     *
     * @param jdbcTemplate шаблон для работы с базой данных Spring JDBC,
     *                     используется для проверки подключения и состояния таблиц.
     *                     Не должен быть {@code null}.
     */
    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Выполняет комплексную проверку работоспособности приложения.
     * <p>
     * Метод выполняет следующие проверки:
     * <ol>
     *   <li>Базовую проверку статуса сервиса</li>
     *   <li>Проверку подключения к базе данных через выполнение тестового SQL-запроса</li>
     *   <li>Проверку существования ключевых таблиц в схеме базы данных</li>
     * </ol>
     * </p>
     * <p>
     * <b>Внимание:</b> В текущей реализации проверяются таблицы USERS, PRODUCTS и TRANSACTIONS.
     * При необходимости этот список может быть расширен или сделав настраиваемым через конфигурацию.
     * </p>
     *
     * @return {@code Map<String, Object>}, содержащая детальную информацию о состоянии системы.
     *         Структура ответа:
     *         <ul>
     *           <li>{@code service} - название сервиса (всегда "bank-recommendation-service")</li>
     *           <li>{@code status} - общий статус сервиса (всегда "running")</li>
     *           <li>{@code timestamp} - время выполнения проверки в миллисекундах</li>
     *           <li>{@code database} - информация о состоянии базы данных:
     *             <ul>
     *               <li>{@code status} - "connected" при успешном подключении или "disconnected" при ошибке</li>
     *               <li>{@code testResult} - результат тестового запроса (1 при успехе)</li>
     *               <li>{@code error} - сообщение об ошибке (только при статусе "disconnected")</li>
     *             </ul>
     *           </li>
     *           <li>{@code tables} - статус наличия ключевых таблиц:
     *             <ul>
     *               <li>Ключ - имя таблицы (USERS, PRODUCTS, TRANSACTIONS)</li>
     *               <li>Значение - {@code true} если таблица существует, {@code false} в противном случае</li>
     *             </ul>
     *           </li>
     *         </ul>
     * @throws org.springframework.dao.DataAccessException при серьезных ошибках доступа к данным
     */
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
