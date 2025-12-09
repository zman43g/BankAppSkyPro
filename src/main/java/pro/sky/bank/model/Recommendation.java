package pro.sky.bank.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Recommendation {
    private String id;
    private String name;
    private String text;

    public Recommendation(String id, String name, String text) {
        this.id = id;
        this.name = name;
        this.text = text;
    }
}

