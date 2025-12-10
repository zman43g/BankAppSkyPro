package pro.sky.bank.controller;

import pro.sky.bank.model.dto.DynamicRuleRequest;
import pro.sky.bank.model.dto.DynamicRuleResponse;
import pro.sky.bank.model.dto.RulesListResponse;
import pro.sky.bank.service.DynamicRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rule")
@RequiredArgsConstructor
public class DynamicRuleController {

    private final DynamicRuleService dynamicRuleService;

    @PostMapping
    public ResponseEntity<DynamicRuleResponse> createRule(@RequestBody DynamicRuleRequest request) {
        DynamicRuleResponse response = dynamicRuleService.createRule(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RulesListResponse> getAllRules() {
        List<DynamicRuleResponse> rules = dynamicRuleService.getAllRules();
        RulesListResponse response = new RulesListResponse();
        response.setData(rules);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteRule(@PathVariable String productId) {
        if (!dynamicRuleService.ruleExists(productId)) {
            return ResponseEntity.notFound().build();
        }

        dynamicRuleService.deleteRule(productId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
    }
}