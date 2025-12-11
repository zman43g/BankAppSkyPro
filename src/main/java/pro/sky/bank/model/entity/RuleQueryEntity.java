package pro.sky.bank.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "rule_queries")
@Data
@NoArgsConstructor
public class RuleQueryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule rule;

    @Column(name = "query_type", nullable = false)
    private String queryType;

    @Column(name = "arguments", length = 1000)
    private String arguments;

    @Column(name = "negate", nullable = false)
    private Boolean negate = false;

}

