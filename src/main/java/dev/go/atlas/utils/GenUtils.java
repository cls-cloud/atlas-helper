package dev.go.atlas.utils;

import dev.go.atlas.entity.GenConfig;
import dev.go.atlas.constant.GenConstants;
import dev.go.atlas.entity.GenTable;
import dev.go.atlas.entity.GenTableColumn;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RegExUtils;

import java.util.Arrays;

/**
 * 代码生成器 工具类
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenUtils {

    /**
     * 初始化表信息
     */
    public static void initTable(GenTable genTable, GenConfig genConfig) {
        genTable.setClassName(convertClassName(genTable.getTableName(), genConfig));
        genTable.setPackageName(genTable.getPackageName());
        genTable.setModuleName(getModuleName(genTable.getPackageName()));
        genTable.setBusinessName(getBusinessName(genTable.getTableName()));
        genTable.setFunctionName(replaceText(genTable.getTableComment()));
        genTable.setFunctionAuthor(genConfig.getAuthor());
    }

    /**
     * 初始化列属性字段
     */
    public static void initColumnField(GenTableColumn column, GenTable table) {
        String dataType = getDbType(column.getColumnType());
        // 统一转小写 避免有些数据库默认大写问题 如果需要特别书写方式 请在实体类增加注解标注别名
        String columnName = column.getColumnName().toLowerCase();
        column.setTableId(table.getTableId());
        // 设置java字段名
        column.setJavaField(StringUtils.toCamelCase(columnName));
        // 设置默认类型
        column.setJavaType(GenConstants.TYPE_STRING);

        column.setGolangType(GenConstants.GO_TYPE_STRING);

        column.setGolangField(formatGoFieldName(columnName));

        column.setQueryType(GenConstants.QUERY_EQ);

        if (arraysContains(GenConstants.COLUMNTYPE_STR, dataType) || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType)) {
            // 字符串长度超过500设置为文本域
            column.setGolangType(GenConstants.GO_TYPE_STRING);
            Integer columnLength = getColumnLength(column.getColumnType());
            String htmlType = columnLength >= 500 || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType) ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT;
            column.setHtmlType(htmlType);
        } else if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType)) {
            column.setJavaType(GenConstants.TYPE_DATE);
            column.setGolangType(GenConstants.GO_TYPE_TIME);
            column.setHtmlType(GenConstants.HTML_DATETIME);
        } else if (arraysContains(GenConstants.COLUMNTYPE_BLOB, dataType)) {
            // ===== 二进制/Blob类型字段 =====
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
            column.setJavaType(GenConstants.TYPE_BYTE_ARRAY);
            column.setGolangType(GenConstants.GO_TYPE_BYTES);
        } else if (arraysContains(GenConstants.COLUMNTYPE_INTEGER, dataType)) {
            column.setHtmlType(GenConstants.HTML_INPUT);
            column.setJavaType(GenConstants.TYPE_LONG);
            String[] precision = getPrecisionScale(column.getColumnType());
            if (precision != null && precision.length == 1 && Integer.parseInt(precision[0]) <= 10) {
                column.setGolangType(GenConstants.GO_TYPE_INT32);
            } else {
                column.setGolangType(GenConstants.GO_TYPE_INT64);
            }
        } else if (arraysContains(GenConstants.COLUMNTYPE_FLOAT, dataType)) {
            column.setHtmlType(GenConstants.HTML_INPUT);
            column.setJavaType(GenConstants.TYPE_BIGDECIMAL);
            column.setGolangType(GenConstants.GO_TYPE_FLOAT64);
        }


        // BO对象 默认插入勾选
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_ADD, columnName) && !column.isPk()) {
            column.setIsInsert(GenConstants.REQUIRE);
        }
        // BO对象 默认编辑勾选
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName)) {
            column.setIsEdit(GenConstants.REQUIRE);
        }
        // VO对象 默认返回勾选
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName)) {
            column.setIsList(GenConstants.REQUIRE);
        }
        // BO对象 默认查询勾选
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !column.isPk()) {
            column.setIsQuery(GenConstants.REQUIRE);
        }

        // 查询字段类型
        if (StringUtils.endsWithIgnoreCase(columnName, "name")) {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }
        // 状态字段设置单选框
        if (StringUtils.endsWithIgnoreCase(columnName, "status")) {
            column.setHtmlType(GenConstants.HTML_RADIO);
        }
        // 类型&性别字段设置下拉框
        else if (StringUtils.endsWithIgnoreCase(columnName, "type")
                || StringUtils.endsWithIgnoreCase(columnName, "sex")) {
            column.setHtmlType(GenConstants.HTML_SELECT);
        }
        // 图片字段设置图片上传控件
        else if (StringUtils.endsWithIgnoreCase(columnName, "image")) {
            column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
        }
        // 文件字段设置文件上传控件
        else if (StringUtils.endsWithIgnoreCase(columnName, "file")) {
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
        }
        // 内容字段设置富文本控件
        else if (StringUtils.endsWithIgnoreCase(columnName, "content")) {
            column.setHtmlType(GenConstants.HTML_EDITOR);
        }
    }

    /**
     * 校验数组是否包含指定值
     *
     * @param arr         数组
     * @param targetValue 值
     * @return 是否包含
     */
    public static boolean arraysContains(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

    private static String[] getPrecisionScale(String columnType) {
        int start = columnType.indexOf("(");
        int end = columnType.indexOf(")");
        if (start > 0 && end > start) {
            return columnType.substring(start + 1, end).split(",");
        }
        return null;
    }

    /**
     * 获取模块名
     *
     * @param packageName 包名
     * @return 模块名
     */
    public static String getModuleName(String packageName) {
        int lastIndex = packageName.lastIndexOf(".");
        int nameLength = packageName.length();
        return StringUtils.substring(packageName, lastIndex + 1, nameLength);
    }

    /**
     * 获取业务名
     *
     * @param tableName 表名
     * @return 业务名
     */
    public static String getBusinessName(String tableName) {
        int firstIndex = tableName.indexOf("_");
        int nameLength = tableName.length();
        String businessName = StringUtils.substring(tableName, firstIndex + 1, nameLength);
        businessName = StringUtils.toCamelCase(businessName);
        return businessName;
    }

    /**
     * 表名转换成Java类名
     *
     * @param tableName 表名称
     * @return 类名
     */
    public static String convertClassName(String tableName, GenConfig genConfig) {
        boolean autoRemovePre = genConfig.getAutoRemovePre();
        String tablePrefix = genConfig.getTablePrefix();
        if (autoRemovePre && StringUtils.isNotEmpty(tablePrefix)) {
            String[] searchList = StringUtils.split(tablePrefix, StringUtils.SEPARATOR);
            tableName = replaceFirst(tableName, searchList);
        }
        return StringUtils.convertToCamelCase(tableName);
    }

    /**
     * 批量替换前缀
     *
     * @param replacementm 替换值
     * @param searchList   替换列表
     */
    public static String replaceFirst(String replacementm, String[] searchList) {
        String text = replacementm;
        for (String searchString : searchList) {
            if (replacementm.startsWith(searchString)) {
                text = replacementm.replaceFirst(searchString, StringUtils.EMPTY);
                break;
            }
        }
        return text;
    }

    /**
     * 关键字替换
     *
     * @param text 需要被替换的名字
     * @return 替换后的名字
     */
    public static String replaceText(String text) {
        return RegExUtils.replaceAll(text, "(?:表|若依)", "");
    }

    /**
     * 获取数据库类型字段
     *
     * @param columnType 列类型
     * @return 截取后的列类型
     */
    public static String getDbType(String columnType) {
        if (StringUtils.indexOf(columnType, "(") > 0) {
            return StringUtils.substringBefore(columnType, "(");
        } else {
            return columnType;
        }
    }

    /**
     * 获取字段长度
     *
     * @param columnType 列类型
     * @return 截取后的列类型
     */
    public static Integer getColumnLength(String columnType) {
        if (StringUtils.indexOf(columnType, "(") > 0) {
            String length = StringUtils.substringBetween(columnType, "(", ")");
            return Integer.valueOf(length);
        } else {
            return 0;
        }
    }

    /**
     * 将数据库字段名转换为符合 Go 命名规则的结构体字段名
     * 例如：
     * user_id -> UserID
     * config_json -> ConfigJSON
     * http_url -> HttpURL
     */
    private static String formatGoFieldName(String columnName) {
        String[] parts = columnName.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.isEmpty(part)) continue;
            String lower = part.toLowerCase();
            // 如果是需要大写的缩写词，则直接大写
            if (arraysContains(GenConstants.GOLANG_UPPER_ABBR, lower)) {
                result.append(lower.toUpperCase());
            } else {
                // 否则正常驼峰化（首字母大写）
                result.append(StringUtils.capitalize(lower));
            }
        }
        return result.toString();
    }

}
