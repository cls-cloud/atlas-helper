package dev.go.atlas.entity;

import dev.go.atlas.constant.GenConstants;
import dev.go.atlas.enums.TemplateType;
import dev.go.atlas.utils.StringUtils;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 业务表信息
 * 对应数据库中的表，用于代码生成
 *
 * @author atlas
 *
 */
@Data
@ToString
public class GenTable {

    /** 表ID */
    private Long tableId;

    /** 数据源名称 */
    private String dataName;

    /** 表名称 */
    private String tableName;

    /** 表描述/备注 */
    private String tableComment;

    /** 关联父表的表名 */
    private String subTableName;

    /** 本表关联父表的外键名 */
    private String subTableFkName;

    /** 实体类名称（首字母大写） */
    private String className;

    /** 使用的模板（crud/tree/sub） */
    private String tplCategory;

    /** 生成包路径 */
    private String packageName;

    /** 生成模块名 */
    private String moduleName;

    /** 生成业务名 */
    private String businessName;

    /** 生成功能名 */
    private String functionName;

    /** 生成作者 */
    private String functionAuthor = "xxx";

    private String email = "xxx@163.com";

    /** 生成代码方式（0=zip压缩包，1=自定义路径） */
    private String genType;

    /** 生成路径（不填默认项目路径） */
    private String genPath;

    /** 主键列信息 */
    private GenTableColumn pkColumn;

    /** 表列信息列表 */
    private List<GenTableColumn> columns;

    /** 其它生成选项 */
    private String options;

    /** 备注信息 */
    private String remark;

    /** 树编码字段 */
    private String treeCode;

    /** 树父编码字段 */
    private String treeParentCode;

    /** 树名称字段 */
    private String treeName;

    /** 菜单ID列表 */
    private List<Long> menuIds;

    /** 上级菜单ID */
    private Long parentMenuId;

    /** 上级菜单名称 */
    private String parentMenuName;

    /*覆盖文件*/
    private Boolean overwrite = true;

    /** 判断是否为树表 */
    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(TemplateType.RuoYiPlusTree.getValue(), tplCategory);
    }

    /** 判断是否为CRUD表 */
    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(TemplateType.RuoYiPlusTree.getValue(), tplCategory);
    }

    /** 判断字段是否为系统列/基础字段 */
    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }

    public static boolean isSuperColumn(String tplCategory, String javaField) {
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }
}
