package dev.go.atlas.constant;

/**
 * 代码生成通用常量
 *
 * @author Lion Li
 */
public interface GenConstants {

    // =============================== 树结构字段 ===============================
    String TREE_CODE = "treeCode";
    String TREE_PARENT_CODE = "treeParentCode";
    String TREE_NAME = "treeName";
    String PARENT_MENU_ID = "parentMenuId";
    String PARENT_MENU_NAME = "parentMenuName";

    // =============================== 数据库类型分类 ===============================
    String[] COLUMNTYPE_STR = {"char", "varchar", "enum", "set", "nchar", "nvarchar", "varchar2", "nvarchar2"};
    String[] COLUMNTYPE_TEXT = {"tinytext", "text", "mediumtext", "longtext", "binary", "varbinary", "blob",
            "ntext", "image", "bytea"};
    String[] COLUMNTYPE_TIME = {"datetime", "time", "date", "timestamp", "year", "interval",
            "smalldatetime", "datetime2", "datetimeoffset", "timestamptz"};
//    String[] COLUMNTYPE_NUMBER = {"tinyint", "smallint", "mediumint", "int", "int2", "int4", "int8", "number", "integer",
//            "bit", "bigint", "float", "float4", "float8", "double", "decimal", "numeric", "real", "double precision",
//            "smallserial", "serial", "bigserial", "money", "smallmoney"};
    /**
     * 数据库整型字段类型
     */
    String[] COLUMNTYPE_INTEGER = {
            "tinyint", "smallint", "mediumint", "int", "int2", "int4", "int8",
            "integer", "bigint", "smallserial", "serial", "bigserial", "bit"
    };

    /**
     * 数据库浮点 / 小数 类型
     */
    String[] COLUMNTYPE_FLOAT = {
            "float", "float4", "float8", "double", "decimal", "numeric", "real",
            "double precision", "money", "smallmoney", "number"
    };
    String[] COLUMNTYPE_BLOB = {
            "blob", "tinyblob", "mediumblob", "longblob",
            "bytea", "binary", "varbinary"
    };


    // =============================== 基础字段控制 ===============================
    String[] COLUMNNAME_NOT_ADD = {"id", "create_dept", "create_by", "create_time", "del_flag", "update_by",
            "update_time", "version", "tenant_id"};
    String[] COLUMNNAME_NOT_EDIT = {"create_dept", "create_by", "create_time", "del_flag", "update_by",
            "update_time", "version", "tenant_id"};
    String[] COLUMNNAME_NOT_LIST = {"create_dept", "create_by", "create_time", "del_flag", "update_by",
            "update_time", "version", "tenant_id"};
    String[] COLUMNNAME_NOT_QUERY = {"id", "create_dept", "create_by", "create_time", "del_flag", "update_by",
            "update_time", "remark", "version", "tenant_id"};

    // =============================== Entity基类字段 ===============================
    String[] BASE_ENTITY = {"createDept", "createBy", "createTime", "updateBy", "updateTime", "tenantId"};

    // =============================== 前端显示类型 ===============================
    String HTML_INPUT = "input";
    String HTML_TEXTAREA = "textarea";
    String HTML_SELECT = "select";
    String HTML_RADIO = "radio";
    String HTML_CHECKBOX = "checkbox";
    String HTML_DATETIME = "datetime";
    String HTML_IMAGE_UPLOAD = "imageUpload";
    String HTML_FILE_UPLOAD = "fileUpload";
    String HTML_EDITOR = "editor";

    // =============================== Java类型常量 ===============================
    String TYPE_STRING = "String";
    String TYPE_INTEGER = "Integer";
    String TYPE_LONG = "Long";
    String TYPE_DOUBLE = "Double";
    String TYPE_BIGDECIMAL = "BigDecimal";
    String TYPE_DATE = "Date";
    String TYPE_BYTE_ARRAY = "byte[]";

    // =============================== Golang类型常量 ===============================
    String GO_TYPE_STRING  = "string";
    String GO_TYPE_BOOL    = "bool";
    String GO_TYPE_INT16   = "int16";
    String GO_TYPE_INT32   = "int32";
    String GO_TYPE_INT64   = "int64";
    String GO_TYPE_FLOAT32 = "float32";
    String GO_TYPE_FLOAT64 = "float64";
    String GO_TYPE_TIME    = "time.Time";
    String GO_TYPE_BYTES = "[]byte";


    String[] GOLANG_UPPER_ABBR = {
            "id", "ip", "url", "json", "xml", "uuid", "http", "https"
    };

    // =============================== 查询方式 ===============================
    String QUERY_LIKE = "LIKE";
    String QUERY_EQ = "EQ";

    // =============================== 是否必填标识 ===============================
    String REQUIRE = "1";
}
