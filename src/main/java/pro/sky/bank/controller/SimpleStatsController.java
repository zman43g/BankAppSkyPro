package pro.sky.bank.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import pro.sky.bank.service.RuleStatisticService;

import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для предоставления системной статистики и административных функций через REST API.
 * <p>
 * Предоставляет эндпоинты для получения:
 * <ul>
 *   <li>Общей и детальной статистики по срабатываниям бизнес-правил</li>
 *   <li>информации о системе (название, версия, время работы)</li>
 *   <li>Выполнения административных операций (очистка кэшей)</li>
 * </ul>
 * </p>
 *
 * @see RuleStatisticService
 * @see GetMapping
 * @see PostMapping
 */
@RestController
public class SimpleStatsController {

    private final RuleStatisticService statisticService;
    private final Instant startTime = Instant.now();

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    /**
     * Конструктор для внедрения зависимости сервиса статистики.
     *
     * @param statisticService сервис для работы со статистикой правил, не должен быть {@code null}.
     */
    public SimpleStatsController(RuleStatisticService statisticService) {
        this.statisticService = statisticService;
    }

    /**
     * Возвращает полную статистику по всем бизнес-правилам.
     * <p>
     * использует {@link RuleStatisticService#getFullStatistics()} для получения агрегированных данных
     * о срабатываниях всех динамических правил в системе.
     * </p>
     *
     * @return {@code Map<String, Object>}, содержащая полную статистику.
     *         Структура карты определяется реализацией {@link RuleStatisticService#getFullStatistics()}.
     * @see RuleStatisticService#getFullStatistics()
     */
    @GetMapping("/rule/stats")
    public Map<String, Object> getRuleStats() {
        return statisticService.getFullStatistics();
    }

    /**
     * Возвращает детальную статистику для конкретного правила по его идентификатору продукта.
     * <p>
     * использует {@link RuleStatisticService#getStatisticByProductId(String)} для получения данных
     * о срабатываниях конкретного правила.
     * </p>
     *
     * @param productId строковый идентификатор продукта (UUID), для которого запрашивается статистика.
     *                  Не должен быть {@code null}.
     * @return {@code Map<String, Object>}, содержащая статистику для указанного правила.
     *         Если правило не найдено, структура ответа зависит от реализации сервиса
     *         (может вернуть пустую карту или карту с информацией об ошибке).
     * @see RuleStatisticService#getStatisticByProductId(String)
     */
    @GetMapping("/rule/stats/{productId}")
    public Map<String, Object> getRuleStat(@PathVariable String productId) {
        return statisticService.getStatisticByProductId(productId);
    }

    /**
     * Выполняет операцию очистки кэшей системы.
     * <p>
     * <b>Внимание:</b> В текущей реализации метод является заглушкой.
     * Для реальной очистки кэшей Spring необходимо использовать {@link org.springframework.cache.CacheManager}.
     * </p>
     *
     * @return {@code Map<String, String>} с результатом операции, содержащая ключи:
     *         <ul>
     *           <li>{@code status} - статус выполнения ("success")</li>
     *           <li>{@code message} - текстовое описание результата</li>
     *           <li>{@code timestamp} - время выполнения операции в формате ISO-8601</li>
     *         </ul>
     */
    @PostMapping("/management/clear-caches")
    public Map<String, String> clearCaches() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Кэши очищены (заглушка)");
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    /**
     * Возвращает основную информацию о системе.
     * <p>
     * Данные загружаются из конфигурации приложения (application.properties/yml)
     * и вычисляются на основе времени запуска приложения.
     * </p>
     *
     * @return {@code Map<String, Object>} с информацией о системе, содержащая ключи:
     *         <ul>
     *           <li>{@code name} - название приложения (из {@code app.name})</li>
     *           <li>{@code version} - версия приложения (из {@code app.version})</li>
     *           <li>{@code uptime} - время работы системы в формате HH:mm:ss</li>
     *           <li>{@code startTime} - время запуска системы в формате ISO-8601</li>
     *         </ul>
     */
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

    /**
     * Форматирует продолжительность времени в читаемый строковый формат HH:mm:ss.
     *
     * @param duration продолжительность времени для форматирования, не должна быть {@code null}.
     * @return строка в формате {@code "часы:минуты:секунды"} с ведущими нулями
     *         (например, "01:05:30" для 1 часа 5 минут 30 секунд).
     */
    private String formatUptime(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}