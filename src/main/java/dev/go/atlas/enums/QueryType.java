package dev.go.atlas.enums;

import lombok.Getter;

/**
 *
 * @author atlas
 *
 */
@Getter
public enum QueryType {
    EQ("EQ", "="),
    NE("NE", "!="),
    LIKE("LIKE", "LIKE"),
    GT("GT", ">"),
    LT("LT", "<"),
    BETWEEN("BETWEEN", "BETWEEN");

    private final String symbol; // 实际存储值
    private final String label;  // 下拉显示值

    QueryType(String symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    // ✅ 下拉框显示中文标签
    @Override
    public String toString() {
        return label;
    }

    public static QueryType fromSymbol(String symbol) {
        for (QueryType q : values()) {
            if (q.symbol.equalsIgnoreCase(symbol)) {
                return q;
            }
        }
        return EQ;
    }
}
