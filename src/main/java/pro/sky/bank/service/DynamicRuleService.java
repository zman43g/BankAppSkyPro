package pro.sky.bank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pro.sky.bank.exception.RuleNotFoundException;
import pro.sky.bank.exception.RuleValidationException;
import pro.sky.bank.model.dto.DynamicRuleRequest;
import pro.sky.bank.model.dto.DynamicRuleResponse;
import pro.sky.bank.model.dto.RuleQuery;
import pro.sky.bank.model.entity.DynamicRule;
import pro.sky.bank.model.entity.RuleQueryEntity;
import pro.sky.bank.repository.DynamicRuleRepository;
import pro.sky.bank.repository.RuleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicRuleService {

    private final DynamicRuleRepository dynamicRuleRepository;
    private final RuleQueryRepository ruleQueryRepository;
    private final ObjectMapper objectMapper;
    private final RuleStatisticService statisticService;

    @Transactional
    public DynamicRuleResponse createRule(DynamicRuleRequest request) {
        validateRuleRequest(request);

        DynamicRule rule = new DynamicRule();
        rule.setProductName(request.getProductName());
        rule.setProductId(request.getProductId());
        rule.setProductText(request.getProductText());

        DynamicRule savedRule = dynamicRuleRepository.save(rule);

        List<RuleQueryEntity> queryEntities = request.getRule().stream()
                .map(query -> createQueryEntity(savedRule, query))
                .collect(Collectors.toList());

        ruleQueryRepository.saveAll(queryEntities);
        savedRule.setQueries(queryEntities);

        // Инициализируем статистику для нового правила
        statisticService.incrementTrigger(savedRule.getProductId(), savedRule.getProductName());

        System.out.println("Created dynamic rule for product: " + request.getProductName());

        return convertToResponse(savedRule);
    }

    @Transactional
    public void deleteRule(String productId) {
        validateProductId(productId);
        DynamicRule rule = findRuleByProductIdOrThrow(productId);

        // Деактивируем статистику перед удалением правила
        statisticService.deactivateStatistic(productId);

        // Удаляем запросы и правило
        deleteRuleAndQueries(rule);
    }

    @Transactional(readOnly = true)
    public List<DynamicRuleResponse> getAllRules() {
        return dynamicRuleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private void deleteRuleAndQueries(DynamicRule rule) {
        ruleQueryRepository.deleteByRule(rule);
        dynamicRuleRepository.delete(rule);
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new RuleValidationException("Product ID cannot be null or empty");
        }

        try {
            UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            throw new RuleValidationException("Product ID must be a valid UUID: " + productId);
        }
    }

    private DynamicRule findRuleByProductIdOrThrow(String productId) {
        return dynamicRuleRepository.findByProductId(productId)
                .orElseThrow(() -> new RuleNotFoundException(
                        "Rule not found for productId: " + productId));
    }


    @Transactional(readOnly = true)
    public boolean ruleExists(String productId) { // Изменили с UUID на String
        return dynamicRuleRepository.existsByProductId(productId);
    }

    private RuleQueryEntity createQueryEntity(DynamicRule rule, RuleQuery query) {
        RuleQueryEntity entity = new RuleQueryEntity();
        entity.setRule(rule);
        entity.setQueryType(query.getQuery());

        try {
            entity.setArguments(objectMapper.writeValueAsString(query.getArguments()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize arguments: " + e.getMessage(), e);
        }

        entity.setNegate(query.getNegate() != null ? query.getNegate() : false);
        return entity;
    }

    private DynamicRuleResponse convertToResponse(DynamicRule rule) {
        DynamicRuleResponse response = new DynamicRuleResponse();
        response.setId(rule.getId());
        response.setProductName(rule.getProductName());
        response.setProductId(rule.getProductId());
        response.setProductText(rule.getProductText());

        // Конвертируем запросы из сущностей в DTO
        List<RuleQuery> queries = rule.getQueries().stream()
                .map(this::convertQueryEntityToDto)
                .collect(Collectors.toList());

        response.setRule(queries);
        return response;
    }

    private RuleQuery convertQueryEntityToDto(RuleQueryEntity entity) {
        RuleQuery query = new RuleQuery();
        query.setQuery(entity.getQueryType());

        try {
            List<String> arguments = objectMapper.readValue(
                    entity.getArguments(),
                    new TypeReference<List<String>>() {
                    }
            );
            query.setArguments(arguments);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize arguments: " + e.getMessage(), e);
        }

        query.setNegate(entity.getNegate());
        return query;
    }

    private void validateRuleRequest(DynamicRuleRequest request) {
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        if (request.getRule() == null || request.getRule().isEmpty()) {
            throw new IllegalArgumentException("Rule must contain at least one query");
        }

        // Проверяем уникальность productId
        if (dynamicRuleRepository.existsByProductId(request.getProductId())) {
            throw new IllegalArgumentException("Rule for this product already exists");
        }
    }
}
