package pro.sky.bank.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DynamicRuleRequest {
    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("product_text")
    private String productText;

    private List<RuleQuery> rule;

}
