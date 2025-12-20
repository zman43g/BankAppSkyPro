package pro.sky.bank.controller;

import pro.sky.bank.exception.RuleNotFoundException;
import pro.sky.bank.model.dto.DynamicRuleRequest;
import pro.sky.bank.model.dto.DynamicRuleResponse;
import pro.sky.bank.model.dto.RulesListResponse;
import pro.sky.bank.service.DynamicRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/rule")
@RequiredArgsConstructor
public class DynamicRuleController {

    private final DynamicRuleService dynamicRuleService;

    /**
     * Создает новое динамическое правило на основе данных из тела запроса.
     * При успешном создании возвращает созданное правило со статусом 201 (Created).
     * Вся бизнес-логика создания и валидации делегируется сервису {@link DynamicRuleService}.
     *
     * @param request DTO-объект {@link DynamicRuleRequest}, содержащий данные для создания правила.
     *                Не должен быть {@code null}. Обязательные поля: productName, productId, rule.
     * @return {@link DynamicRuleResponse} — DTO-объект созданного правила.
     * @throws IllegalArgumentException если запрос не прошел валидацию (отсутствуют обязательные поля,
     *         productId не является валидным UUID или правило с таким productId уже существует).
     * @see DynamicRuleService#createRule(DynamicRuleRequest)
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DynamicRuleResponse createRule(@RequestBody DynamicRuleRequest request) {
        return dynamicRuleService.createRule(request);
    }

    /**
     * Возвращает список всех динамических правил, существующих в системе.
     * Ответ оборачивается в объект {@link RulesListResponse} для единообразной структуры ответа API.
     * Возвращает статус 200 (OK) даже если список пуст.
     * @return {@link ResponseEntity} со статусом OK и телом типа {@link RulesListResponse},
     *         содержащим список {@link DynamicRuleResponse}.
     */
    @GetMapping
    public ResponseEntity<RulesListResponse> getAllRules() {
        List<DynamicRuleResponse> rules = dynamicRuleService.getAllRules();
        RulesListResponse response = new RulesListResponse();
        response.setData(rules);
        return ResponseEntity.ok(response);
    }
    /**
     * Удаляет динамическое правило по его бизнес-идентификатору (productId).
     * При успешном удалении возвращает статус 204 (No Content). Также удаляет всю связанную статистику
     * и вложенные запросы (RuleQuery) для данного правила.
     * @param productId Строковый идентификатор продукта (должен быть валидным UUID), правило для которого
     *                  требуется удалить. Не должен быть {@code null} или пустым.
     * @throws RuleNotFoundException если правило с указанным {@code productId} не найдено в системе.
     */
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable String productId) {
        dynamicRuleService.deleteRule(productId);
    }


}