package pro.sky.bank.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RuleQuery {
    private String query;
    private List<String> arguments;
    private Boolean negate;

    public enum QueryType {
        USER_OF,
        ACTIVE_USER_OF,
        TRANSACTION_SUM_COMPARE,
        TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW
    }

    public enum ComparisonOperator {
        GREATER(">"),
        LESS("<"),
        EQUAL("="),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<=");

        private final String symbol;

        ComparisonOperator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static ComparisonOperator fromSymbol(String symbol) {
            for (ComparisonOperator op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public void setNegate(Boolean negate) {
        this.negate = negate;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public Boolean getNegate() {
        return negate;
    }
}
