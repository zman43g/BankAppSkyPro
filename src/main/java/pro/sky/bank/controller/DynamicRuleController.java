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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DynamicRuleResponse createRule(@RequestBody DynamicRuleRequest request) {
        return dynamicRuleService.createRule(request);
    }

    @GetMapping
    public ResponseEntity<RulesListResponse> getAllRules() {
        List<DynamicRuleResponse> rules = dynamicRuleService.getAllRules();
        RulesListResponse response = new RulesListResponse();
        response.setData(rules);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable String productId) {
        dynamicRuleService.deleteRule(productId);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
    }
}