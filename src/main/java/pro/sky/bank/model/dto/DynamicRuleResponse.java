package pro.sky.bank.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DynamicRuleResponse {
    private Long id;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("product_text")
    private String productText;

    private List<RuleQuery> rule;

}
