package dev.go.atlas.entity;

import dev.go.atlas.utils.StringUtils;
import lombok.Data;

/**
 * 代码生成业务字段
 * 对应表字段信息，用于代码生成
 */
@Data
public class GenTableColumn {

    /** 列ID */
    private Long columnId;

    /** 所属表ID */
    private Long tableId;

    /** 列名称（数据库字段名或展示名称） */
    private String columnName;

    /** 列描述/备注 */
    private String columnComment;

    /** 数据库列类型 */
    private String columnType;

    /** Java 类型 */
    private String javaType;

    /** Java 字段名 */
    private String javaField;

    /** Golang 类型 */
    private String golangType;

    /** Golang 字段名 */
    private String golangField;

    /** 是否主键：1=是，0=否 */
    private String isPk;

    /** 是否自增：1=是，0=否 */
    private String isIncrement;

    /** 是否必填：1=是，0=否 */
    private String isRequired;

    /** 是否插入字段：1=是，0=否 */
    private String isInsert;

    /** 是否编辑字段：1=是，0=否 */
    private String isEdit;

    /** 是否列表字段：1=是，0=否 */
    private String isList;

    /** 是否查询字段：1=是，0=否 */
    private String isQuery;

    /** 查询方式（EQ=等于, LIKE=模糊, GT=大于, LT=小于 等） */
    private String queryType;

    /** 前端显示类型（input, textarea, select, checkbox, radio, datetime 等） */
    private String htmlType;

    /** 字典类型 */
    private String dictType;

    /** 排序 */
    private Integer sort;

    /** 获取首字母大写的 Java 字段名 */
    public String getCapJavaField() {
        return StringUtils.capitalize(javaField);
    }

    /** 是否主键 */
    public boolean isPk() {
        return "1".equals(isPk);
    }

    /** 是否自增 */
    public boolean isIncrement() {
        return "1".equals(isIncrement);
    }

    /** 是否必填 */
    public boolean isRequired() {
        return "1".equals(isRequired);
    }

    /** 是否插入字段 */
    public boolean isInsert() {
        return "1".equals(isInsert);
    }

    /** 是否编辑字段 */
    public boolean isEdit() {
        return "1".equals(isEdit);
    }

    /** 是否列表字段 */
    public boolean isList() {
        return "1".equals(isList);
    }

    /** 是否查询字段 */
    public boolean isQuery() {
        return "1".equals(isQuery);
    }

    /** 是否为系统列/超级字段 */
    public boolean isSuperColumn() {
        return StringUtils.equalsAnyIgnoreCase(javaField, "createBy", "createTime", "updateBy", "updateTime", "parentName", "parentId");
    }

    /** 是否为可用列（用于生成页面字段） */
    public boolean isUsableColumn() {
        return StringUtils.equalsAnyIgnoreCase(javaField, "parentId", "orderNum", "remark");
    }

    /** 解析列备注，生成前端转换表达式 */
    public String readConverterExp() {
        String remarks = StringUtils.substringBetween(this.columnComment, "（", "）");
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotEmpty(remarks)) {
            for (String value : remarks.split(" ")) {
                if (StringUtils.isNotEmpty(value)) {
                    Object startStr = value.subSequence(0, 1);
                    String endStr = value.substring(1);
                    sb.append(StringUtils.EMPTY).append(startStr).append("=").append(endStr).append(StringUtils.SEPARATOR);
                }
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            return this.columnComment;
        }
    }
}
