package dev.go.atlas.dialog;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.JBIterable;
import dev.go.atlas.entity.GenConfig;
import dev.go.atlas.entity.GenTable;
import dev.go.atlas.entity.GenTableColumn;
import dev.go.atlas.ui.CodeGeneratorUI;
import dev.go.atlas.utils.GenUtils;
import dev.go.atlas.utils.StringUtils;
import dev.go.atlas.utils.VelocityUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 *
 * @author atlas
 *
 */
public class DatabaseDialogWrapper extends DialogWrapper {
    private final AnActionEvent e;
    private final Project project;
    private CodeGeneratorUI ui;
    private GenTable genTable;

    public DatabaseDialogWrapper(AnActionEvent e, Project project) {

        super(true);

        this.e = e;

        this.project = project;

        this.genTable = getTableInfos(e);

        init();

        setTitle("Generate Code");
    }

    @Nullable
    protected JComponent createCenterPanel() {

        this.ui = new CodeGeneratorUI(this.project, this.genTable);

        return this.ui.getContainer();
    }
    private boolean validationShown = false;
    @Nullable
    protected ValidationInfo doValidate() {
        if (!validationShown && this.ui.checkParams(this.project)) {
            validationShown = true;  // 设置标记，表示已经显示过提示
            return new ValidationInfo("校验不通过");
        }
        return null;
    }

    protected void doOKAction() {
        super.doOKAction();
        GenTable param = this.ui.getGenTable();
        generate(this.e, param);
    }

    protected void generate(AnActionEvent e, GenTable table) {
        Project project = e.getProject();
        if (project == null) return;
        setPkColumn(table);
        VelocityContext context = VelocityUtils.prepareContext(table);
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getDataName(), table.getFunctionAuthor());
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                for (String templatePath : templates) {
                    try {
                        String base = "http://helper.go-atlas.cn/";
                        String templateUrl = base + templatePath;
                        String templateContent;
                        try (InputStream in = new URI(templateUrl).toURL().openStream()) {
                            templateContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        }
                        Properties p = new Properties();
                        p.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
                        Velocity.init(p);
                        StringWriter sw = new StringWriter();
                        Velocity.evaluate(context, sw, templateUrl, new StringReader(templateContent));
                        String result = sw.toString();
                        String pathStr = getGenPath(project, table, templatePath);
                        Path path = Path.of(pathStr);
                        if (!Files.exists(path.getParent())) {
                            Files.createDirectories(path.getParent());
                        }
                        Files.writeString(path, result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (Exception ex) {
                        System.out.println("[ERROR]: "+ ex.getMessage());
                        throw new RuntimeException("渲染模板失败，表名：" + table.getTableName());
                    }
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    VirtualFileManager.getInstance().syncRefresh();
                    Messages.showInfoMessage(project, "Generate code is ok", "Success");
                });
            } catch (Exception ex) {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showWarningDialog(project, ex.getMessage(), "Error"));
                ex.printStackTrace();
            }
        });
    }

    /**
     * 获取代码生成地址
     *
     * @param table    业务表信息
     * @param template 模板文件路径
     * @return 生成地址
     */
    public static String getGenPath(Project project, GenTable table, String template) {
        String genPath = table.getGenPath();
        if (StrUtil.isBlank(genPath)) {
            VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
            if (baseDir != null) {
                String projectPath = baseDir.getPath(); // 项目根目录
                genPath = projectPath + File.separator + "gen";
            }
        }
        if (StringUtils.equals(genPath, "/")) {
            return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
        }
        return genPath + File.separator + VelocityUtils.getFileName(template, table);
    }

    /**
     * 设置主键列信息
     *
     * @param table 业务表信息
     */
    public void setPkColumn(GenTable table) {
        for (GenTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }
        if (ObjectUtil.isNull(table.getPkColumn())) {
            table.setPkColumn(table.getColumns().getFirst());
        }

    }

    @Nullable
    private GenTable getTableInfos(AnActionEvent e) {
        PsiElement[] tableElements = (PsiElement[]) e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (tableElements == null) {
            return null;
        }

        List<DbTable> dbTables = Stream.<PsiElement>of(tableElements).filter(t -> t instanceof DbTable).map(t -> (DbTable) t).toList();
        DbTable dbTable = dbTables.getFirst();
        String tableName = dbTable.getName();
        String comment = dbTable.getComment();
        String tComment = (comment == null || comment.isEmpty()) ? tableName : comment;
        String dbType = getDatabaseType(dbTable);

        GenTable genTable = new GenTable();
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(snowflake.nextId());
        }
        genTable.setMenuIds(menuIds);
        genTable.setTableComment(tComment);
        genTable.setTableName(tableName);
        genTable.setDataName(dbType);
        genTable.setPackageName("dev.go.atlas.system");
        GenConfig genConfig = new GenConfig();
        GenUtils.initTable(genTable, genConfig);


        DasTable dasObject = dbTable.getDasObject();
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns((DasObject) dasObject);

        List<GenTableColumn> tableColumns = new ArrayList<>();
        int sort = 1;
        for (DasColumn column : columns) {
            String columnName = column.getName();
            String dataType = column.getDasType().getTypeClass().getName();
            String columnComment = column.getComment();

            GenTableColumn genTableColumn = new GenTableColumn();
            genTableColumn.setColumnComment(columnComment);
            genTableColumn.setColumnName(columnName);
            genTableColumn.setColumnType(dataType);
            genTableColumn.setSort(sort);
            GenUtils.initColumnField(genTableColumn, genTable);
            tableColumns.add(genTableColumn);
            sort++;
        }
        genTable.setColumns(tableColumns);

        return genTable;
    }

    private String getDatabaseType(DbTable dbTable) {
        if (dbTable == null) {
            return "unknown";
        }
        var dataSource = dbTable.getDataSource();
        if (dataSource == null) {
            return "unknown";
        }
        String url = dataSource.getConnectionConfig().getUrl().toLowerCase();
        if (url.startsWith("jdbc:mysql")) {
            return "mysql";
        } else if (url.startsWith("jdbc:postgresql")) {
            return "postgresql";
        } else if (url.startsWith("jdbc:sqlserver")) {
            return "sqlserver";
        } else if (url.startsWith("jdbc:oracle")) {
            return "oracle";
        } else if (url.startsWith("jdbc:sqlite")) {
            return "sqlite";
        }

        return "unknown";
    }


}
