package pro.sky.bank.model.dto;

import lombok.Data;


import java.util.List;

@Data
public class RulesListResponse {
    private List<DynamicRuleResponse> data;

    public RulesListResponse(List<DynamicRuleResponse> data) {
    }

    public RulesListResponse() {

    }


    public void setData(List<DynamicRuleResponse> data) {
        this.data = data;
    }
}
