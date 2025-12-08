package pro.sky.bank.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RecommendationResponse {
    private String userId;
    private List<Recommendation> recommendations;

    public RecommendationResponse(String userId, List<Recommendation> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
    }
}
