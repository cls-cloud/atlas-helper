package dev.go.atlas.enums;

import lombok.Getter;

/**
 *
 * @author atlas
 *
 */
@Getter
public enum TemplateType {
    RuoYiVue2("RuoYi(Vue2):curd", "ruoyi-vue2"),
    RuoYiVue3("RuoYi(Vue3):curd", "ruoyi-vue3"),
    RuoYiPlus("RuoYi-Plus:curd", "ruoyi-plus"),
    RuoYiPlusTree("RuoYi-Plus:Tree", "ruoyi-plus-tree"),
    GoAtlas("GoAtlas:curd", "go-atlas"),
    GoZero("GoZero:curd", "go-zero"),
    GoKratos("GoKratos:crud", "go-kratos");


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
