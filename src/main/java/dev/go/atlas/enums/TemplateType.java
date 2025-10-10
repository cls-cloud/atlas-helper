package dev.go.atlas.enums;

import lombok.Getter;

@Getter
public enum TemplateType {
    GoAtlas("GoAtlas:curd", "go-atlas"),
    GoZero("GoZero:curd", "go-zero"),
    GoKratos("GoKratos:crud", "go-kratos"),
    JavaCrud("Java:crud", "crud"),
    JavaTree("Java:tree", "tree");

    private final String label;
    private final String value;

    TemplateType(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String toString() {
        return label; // 下拉框显示 label
    }

    public static TemplateType fromValue(String value) {
        for (TemplateType t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        return GoZero;
    }
}
