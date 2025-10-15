package dev.go.atlas.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import dev.go.atlas.constant.GenConstants;
import dev.go.atlas.entity.GenTable;
import dev.go.atlas.entity.GenTableColumn;
import dev.go.atlas.enums.TemplateType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.velocity.VelocityContext;

import java.util.*;

/**
 * 模板处理工具类
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VelocityUtils {

    /**
     * 项目空间路径
     */
    private static final String PROJECT_PATH = "main/java";

    /**
     * mybatis空间路径
     */
    private static final String MYBATIS_PATH = "main/resources/mapper";

    /**
     * 默认上级菜单，系统工具
     */
    private static final String DEFAULT_PARENT_MENU_ID = "3";

    /**
     * 设置模板变量信息
     *
     * @return 模板列表
     */
    public static VelocityContext prepareContext(GenTable genTable) {
        String moduleName = genTable.getModuleName();
        String businessName = genTable.getBusinessName();
        String packageName = genTable.getPackageName();
        String tplCategory = genTable.getTplCategory();
        String functionName = genTable.getFunctionName();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tplCategory", genTable.getTplCategory());
        velocityContext.put("tableName", genTable.getTableName());
        velocityContext.put("functionName", StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
        velocityContext.put("ClassName", genTable.getClassName());
        velocityContext.put("className", StringUtils.uncapitalize(genTable.getClassName()));
        velocityContext.put("moduleName", genTable.getModuleName());
        velocityContext.put("BusinessName", StringUtils.capitalize(genTable.getBusinessName()));
        velocityContext.put("businessName", genTable.getBusinessName());
        velocityContext.put("basePackage", getPackagePrefix(packageName));
        velocityContext.put("packageName", packageName);
        velocityContext.put("author", genTable.getFunctionAuthor());
        velocityContext.put("email", genTable.getEmail());
        velocityContext.put("datetime", DateUtils.getDate());
        velocityContext.put("currentDate", DateUtils.getTime());
        velocityContext.put("pkColumn", genTable.getPkColumn());
        velocityContext.put("importList", getImportList(genTable));
        velocityContext.put("permissionPrefix", getPermissionPrefix(moduleName, businessName));
        velocityContext.put("columns", genTable.getColumns());
        velocityContext.put("table", genTable);
        velocityContext.put("dicts", getDicts(genTable));
        setMenuVelocityContext(velocityContext, genTable);
        if (TemplateType.RuoYiPlusTree.getValue().equals(tplCategory)) {
            setTreeVelocityContext(velocityContext, genTable);
        }
        // 判断是modal还是drawer
        Dict paramsObj = JsonUtils.parseMap(genTable.getOptions());
        if (ObjectUtil.isNotNull(paramsObj)) {
            String popupComponent = Optional
                    .ofNullable(paramsObj.getStr("popupComponent"))
                    .orElse("modal");
            velocityContext.put("popupComponent", popupComponent);
            velocityContext.put("PopupComponent", StringUtils.capitalize(popupComponent));
        } else {
            velocityContext.put("popupComponent", "modal");
            velocityContext.put("PopupComponent", "Modal");
        }
        // 判断是原生antd表单还是useForm表单
        // native 原生antd表单
        // useForm useVbenForm
        if (ObjectUtil.isNotNull(paramsObj)) {
            String formComponent = Optional
                    .ofNullable(paramsObj.getStr("formComponent"))
                    .orElse("useForm");
            velocityContext.put("formComponent", formComponent);
        } else {
            velocityContext.put("formComponent", "useForm");
        }
        return velocityContext;
    }

    public static void setMenuVelocityContext(VelocityContext context, GenTable genTable) {
        String options = genTable.getOptions();
        Dict paramsObj = JsonUtils.parseMap(options);
        String parentMenuId = getParentMenuId(paramsObj);
        context.put("parentMenuId", parentMenuId);
    }

    public static void setTreeVelocityContext(VelocityContext context, GenTable genTable) {
        String options = genTable.getOptions();
        Dict paramsObj = JsonUtils.parseMap(options);
        String treeCode = getTreecode(paramsObj);
        String treeParentCode = getTreeParentCode(paramsObj);
        String treeName = getTreeName(paramsObj);

        context.put("treeCode", treeCode);
        context.put("treeParentCode", treeParentCode);
        context.put("treeName", treeName);
        context.put("expandColumn", getExpandColumn(genTable));
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_CODE)) {
            context.put("tree_parent_code", paramsObj.get(GenConstants.TREE_PARENT_CODE));
        }
        if (paramsObj.containsKey(GenConstants.TREE_NAME)) {
            context.put("tree_name", paramsObj.get(GenConstants.TREE_NAME));
        }
    }


    /**
     * 获取模板信息
     *
     * @return 模板列表
     */
    public static List<String> getTemplateList(String tplCategory, String dbName, String author) {
        Set<String> specialAuthors = Set.of("Atlas", "Orion");
        List<String> templates = new ArrayList<>();

        boolean includeVben5 = author != null && specialAuthors.contains(author);
        TemplateType templateType = TemplateType.fromValue(tplCategory);
        switch (templateType) {
            case TemplateType.GoAtlas -> {
                templates.add("vm/go/desc.api.vm");
                addRuoYiPlusSqlTemplate(templates, dbName);
                if (includeVben5) addVben5Templates(templates);
            }
            case TemplateType.GoZero -> {
                templates.add("vm/go/desc.api.vm");
                addRuoYiPlusSqlTemplate(templates, dbName);
            }
            case TemplateType.GoKratos -> { /* 暂无模板 */ }
            case TemplateType.RuoYiPlus -> {
                addRuoYiPlusCommonTemplates(templates);
                addRuoYiPlusSqlTemplate(templates, dbName);
                templates.add("vm/ruoyi-plus/vue/index.vue.vm");
                if (includeVben5) addVben5Templates(templates);
            }
            case TemplateType.RuoYiPlusTree -> {
                addRuoYiPlusCommonTemplates(templates);
                addRuoYiPlusSqlTemplate(templates, dbName);
                templates.add("vm/ruoyi-plus/vue/index-tree.vue.vm");
                if (includeVben5) addVben5Templates(templates);
            }
            case TemplateType.RuoYiVue2 -> {
                addRuoYiCommonTemplates(templates, TemplateType.RuoYiVue2);
            }
            case TemplateType.RuoYiVue3 -> {
                addRuoYiCommonTemplates(templates, TemplateType.RuoYiVue3);
            }
            default -> {
                addRuoYiPlusSqlTemplate(templates, dbName);
            }
        }

        return templates;
    }


    private static void addRuoYiCommonTemplates(List<String> templates, TemplateType templateType) {
        templates.add("vm/ruoyi/java/domain.java.vm");
        templates.add("vm/ruoyi/java/mapper.java.vm");
        templates.add("vm/ruoyi/java/service.java.vm");
        templates.add("vm/ruoyi/java/serviceImpl.java.vm");
        templates.add("vm/ruoyi/java/controller.java.vm");
        templates.add("vm/ruoyi/xml/mapper.xml.vm");
        templates.add("vm/ruoyi/sql/sql.vm");
        templates.add("vm/ruoyi/js/api.js.vm");
        if (templateType == TemplateType.RuoYiVue2) {
            templates.add("vm/ruoyi/vue/index.vue.vm");
        }else if (templateType == TemplateType.RuoYiVue3) {
            templates.add("vm/ruoyi/vue/v3/index-tree.vue.vm");
        }
    }

    private static void addRuoYiPlusCommonTemplates(List<String> templates) {
        templates.addAll(List.of(
                "vm/ruoyi-plus/java/domain.java.vm",
                "vm/ruoyi-plus/java/vo.java.vm",
                "vm/ruoyi-plus/java/bo.java.vm",
                "vm/ruoyi-plus/java/mapper.java.vm",
                "vm/ruoyi-plus/java/service.java.vm",
                "vm/ruoyi-plus/java/serviceImpl.java.vm",
                "vm/ruoyi-plus/java/controller.java.vm",
                "vm/ruoyi-plus/xml/mapper.xml.vm"
        ));
        templates.addAll(List.of(
                "vm/ruoyi-plus/sql/sql.vm",
                "vm/ruoyi-plus/ts/api.ts.vm",
                "vm/ruoyi-plus/ts/types.ts.vm"
        ));
    }

    private static void addRuoYiPlusSqlTemplate(List<String> templates, String dbName) {
        switch (dbName) {
            case "oracle" -> templates.add("vm/ruoyi-plus/sql/oracle/sql.vm");
            case "postgresql" -> templates.add("vm/ruoyi-plus/sql/postgres/sql.vm");
            case "sqlserver" -> templates.add("vm/ruoyi-plus/sql/sqlserver/sql.vm");
            default -> templates.add("vm/ruoyi-plus/sql/sql.vm");
        }
    }

    private static void addVben5Templates(List<String> templates) {
        templates.addAll(List.of(
                "vm/vben5/api/index.ts.vm",
                "vm/vben5/api/model.d.ts.vm",
                "vm/vben5/views/data.ts.vm",
                "vm/vben5/views/index_vben.vue.vm",
                "vm/vben5/views/popup.vue.vm"
        ));
    }


    /**
     * 获取文件名
     */
    public static String getFileName(String template, GenTable genTable) {
        // 文件名称
        String fileName = "";
        // 包路径
        String packageName = genTable.getPackageName();
        // 模块名
        String moduleName = genTable.getModuleName();
        // 大写类名
        String className = genTable.getClassName();
        // 业务名称
        String businessName = genTable.getBusinessName();

        String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/");
        String mybatisPath = MYBATIS_PATH + "/" + moduleName;
        String vuePath = "vue";
        String goPath = moduleName;


        if (template.contains("domain.java.vm")) {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        }
        if (template.contains("vo.java.vm")) {
            fileName = StringUtils.format("{}/domain/vo/{}Vo.java", javaPath, className);
        }
        if (template.contains("bo.java.vm")) {
            fileName = StringUtils.format("{}/domain/bo/{}Bo.java", javaPath, className);
        }
        if (template.contains("mapper.java.vm")) {
            fileName = StringUtils.format("{}/mapper/{}Mapper.java", javaPath, className);
        } else if (template.contains("service.java.vm")) {
            fileName = StringUtils.format("{}/service/I{}Service.java", javaPath, className);
        } else if (template.contains("serviceImpl.java.vm")) {
            fileName = StringUtils.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        } else if (template.contains("controller.java.vm")) {
            fileName = StringUtils.format("{}/controller/{}Controller.java", javaPath, className);
        } else if (template.contains("mapper.xml.vm")) {
            fileName = StringUtils.format("{}/{}Mapper.xml", mybatisPath, className);
        } else if (template.contains("sql.vm")) {
            fileName = businessName + "Menu.sql";
        } else if (template.contains("api.ts.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/index.ts", vuePath, moduleName, businessName);
        } else if (template.contains("types.ts.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/types.ts", vuePath, moduleName, businessName);
        } else if (template.contains("index.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        } else if (template.contains("index-tree.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        }

        // 判断是modal还是drawer
        Dict paramsObj = JsonUtils.parseMap(genTable.getOptions());
        String popupComponent = "modal";
        if (ObjectUtil.isNotNull(paramsObj)) {
            popupComponent = Optional
                    .ofNullable(paramsObj.getStr("popupComponent"))
                    .orElse("modal");
        }
        String vben5Path = "vben5";
        if (template.contains("vm/vben5/api/index.ts.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/index.ts", vben5Path, moduleName, businessName);
        }
        if (template.contains("vm/vben5/api/model.d.ts.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/model.d.ts", vben5Path, moduleName, businessName);
        }
        if (template.contains("vm/vben5/views/index_vben.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vben5Path, moduleName, businessName);
        }
        if (template.contains("vm/vben5/views/index_vben_tree.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vben5Path, moduleName, businessName);
        }
        if (template.contains("vm/vben5/views/data.ts.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/data.ts", vben5Path, moduleName, businessName);
        }
        if (template.contains("vm/vben5/views/popup.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/{}-{}.vue", vben5Path, moduleName, businessName, businessName, popupComponent);
        }
        if (template.contains("vm/vben5/views/popup_tree.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/{}-{}.vue", vben5Path, moduleName, businessName, businessName, popupComponent);
        }

        if (template.contains("api.js.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/index.js", vuePath, moduleName, businessName);
        }
        // ================= Go 文件 =================
        if (template.contains("go/desc.api.vm")) {
            fileName = StringUtils.format("{}/api/{}.api", goPath, businessName);
        }

        return fileName;
    }

    /**
     * 获取包前缀
     *
     * @param packageName 包名称
     * @return 包前缀名称
     */
    public static String getPackagePrefix(String packageName) {
        int lastIndex = packageName.lastIndexOf(".");
        return StringUtils.substring(packageName, 0, lastIndex);
    }

    /**
     * 根据列类型获取导入包
     *
     * @param genTable 业务表对象
     * @return 返回需要导入的包列表
     */
    public static HashSet<String> getImportList(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();
        HashSet<String> importList = new HashSet<>();
        for (GenTableColumn column : columns) {
            if (!column.isSuperColumn() && GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                importList.add("java.util.Date");
                importList.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if (!column.isSuperColumn() && GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                importList.add("java.math.BigDecimal");
            } else if (!column.isSuperColumn() && "imageUpload".equals(column.getHtmlType())) {
                importList.add("org.dromara.common.translation.annotation.Translation");
                importList.add("org.dromara.common.translation.constant.TransConstant");
            }
        }
        return importList;
    }

    /**
     * 根据列类型获取字典组
     *
     * @param genTable 业务表对象
     * @return 返回字典组
     */
    public static String getDicts(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();
        Set<String> dicts = new HashSet<>();
        addDicts(dicts, columns);
        return StringUtils.join(dicts, ", ");
    }

    /**
     * 添加字典列表
     *
     * @param dicts   字典列表
     * @param columns 列集合
     */
    public static void addDicts(Set<String> dicts, List<GenTableColumn> columns) {
        for (GenTableColumn column : columns) {
            if (!column.isSuperColumn() && StringUtils.isNotEmpty(column.getDictType()) && StringUtils.equalsAny(
                    column.getHtmlType(),
                    new String[]{GenConstants.HTML_SELECT, GenConstants.HTML_RADIO, GenConstants.HTML_CHECKBOX})) {
                dicts.add("'" + column.getDictType() + "'");
            }
        }
    }

    /**
     * 获取权限前缀
     *
     * @param moduleName   模块名称
     * @param businessName 业务名称
     * @return 返回权限前缀
     */
    public static String getPermissionPrefix(String moduleName, String businessName) {
        return StringUtils.format("{}:{}", moduleName, businessName);
    }

    /**
     * 获取上级菜单ID字段
     *
     * @param paramsObj 生成其他选项
     * @return 上级菜单ID字段
     */
    public static String getParentMenuId(Dict paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.PARENT_MENU_ID)
                && StringUtils.isNotEmpty(paramsObj.getStr(GenConstants.PARENT_MENU_ID))) {
            return paramsObj.getStr(GenConstants.PARENT_MENU_ID);
        }
        return DEFAULT_PARENT_MENU_ID;
    }

    /**
     * 获取树编码
     *
     * @param paramsObj 生成其他选项
     * @return 树编码
     */
    public static String getTreecode(Map<String, Object> paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_CODE)) {
            return StringUtils.toCamelCase(Convert.toStr(paramsObj.get(GenConstants.TREE_CODE)));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取树父编码
     *
     * @param paramsObj 生成其他选项
     * @return 树父编码
     */
    public static String getTreeParentCode(Dict paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_PARENT_CODE)) {
            return StringUtils.toCamelCase(paramsObj.getStr(GenConstants.TREE_PARENT_CODE));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取树名称
     *
     * @param paramsObj 生成其他选项
     * @return 树名称
     */
    public static String getTreeName(Dict paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_NAME)) {
            return StringUtils.toCamelCase(paramsObj.getStr(GenConstants.TREE_NAME));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取需要在哪一列上面显示展开按钮
     *
     * @param genTable 业务表对象
     * @return 展开按钮列序号
     */
    public static int getExpandColumn(GenTable genTable) {
        String options = genTable.getOptions();
        Dict paramsObj = JsonUtils.parseMap(options);
        String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
        int num = 0;
        for (GenTableColumn column : genTable.getColumns()) {
            if (column.isList()) {
                num++;
                String columnName = column.getColumnName();
                if (columnName.equals(treeName)) {
                    break;
                }
            }
        }
        return num;
    }
}
