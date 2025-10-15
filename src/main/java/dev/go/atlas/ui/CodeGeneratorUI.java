package dev.go.atlas.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import dev.go.atlas.entity.GenTable;
import dev.go.atlas.entity.GenTableColumn;
import dev.go.atlas.enums.QueryType;
import dev.go.atlas.enums.TemplateType;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorUI {
    @Getter
    private final JPanel container = new JPanel(new BorderLayout());
    // 基础信息字段
    private final JTextField tableNameField = new JTextField();
    private final JTextField tableCommentField = new JTextField();
    private final JTextField classNameField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField packageField = new JTextField();
    private final JTextField moduleField = new JTextField();
    private final JTextField businessNameField = new JTextField();
    private final JTextField functionNameField = new JTextField();
    private final JTextField outputPathField = new JTextField();
    private final JButton chooseBtn = new JButton("选择");
    private final JCheckBox overwriteCheck = new JCheckBox("覆盖已有文件", true);
    private JComboBox<TemplateType> templateCombo;
    private JTable fieldTable;
    private GenTable config;

    public CodeGeneratorUI() {
        initContainer();
    }

    public CodeGeneratorUI(Project project, GenTable tableInfo) {
        initContainer();
        this.config = tableInfo == null ? new GenTable() : tableInfo;
        loadConfig(config);
        if (project != null) setListener(project);
    }

    public GenTable getGenTable() {
        return collectGenTable();
    }

    private void initContainer() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("基础配置", createBaseInfoPanel());
        tabbedPane.addTab("字段信息", createFieldPanel());

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        container.add(tabbedPane, BorderLayout.CENTER);
        container.add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * 基础配置面板
     */
    /**
     * 基础配置面板（左右各占 50%）
     */
    private JPanel createBaseInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 左右两列面板
        JPanel leftPanel = new JPanel(new GridBagLayout());
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        int row = 0;
        // ===== 左侧：基础字段 =====
        addLabelAndField(leftPanel, "表名称", tableNameField, 0, row++, gbc);
        addLabelAndField(leftPanel, "实体类名", classNameField, 0, row++, gbc);

        templateCombo = new JComboBox<>(TemplateType.values());
        templateCombo.setSelectedItem(TemplateType.GoAtlas);
        addLabelAndField(leftPanel, "生成模板", templateCombo, 0, row++, gbc);
        addLabelAndField(leftPanel, "模块名", moduleField, 0, row++, gbc);
        addLabelAndField(leftPanel, "功能名", functionNameField, 0, row++, gbc);

        // ===== 右侧：附加信息 =====
        row = 0;
        addLabelAndField(rightPanel, "表描述", tableCommentField, 0, row++, gbc);
        addLabelAndField(rightPanel, "作者", authorField, 0, row++, gbc);
        addLabelAndField(rightPanel, "生成包路径", packageField, 0, row++, gbc);
        addLabelAndField(rightPanel, "业务名", businessNameField, 0, row++, gbc);

        JPanel overwritePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        overwritePanel.add(overwriteCheck);
        addLabelAndField(rightPanel, "覆盖已有文件", overwritePanel, 0, row++, gbc);

        // ===== 将左右面板放入中间GridLayout（各50%） =====
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // ===== 底部：生成路径选择 =====
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        bottomPanel.add(new JLabel("代码生成路径："), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
        pathPanel.add(outputPathField, BorderLayout.CENTER);
        pathPanel.add(chooseBtn, BorderLayout.EAST);
        bottomPanel.add(pathPanel, gbc);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /** 文件选择事件 */
    private void setListener(Project project) {
        chooseBtn.addActionListener(e -> chooseOutputPath(project));
    }
    /**
     * 工具函数 - 添加标签和输入控件到网格布局
     */
    private void addLabelAndField(JPanel panel, String label, JComponent field, int col, int row, GridBagConstraints gbc) {
        int gridx = col * 2;
        gbc.gridx = gridx;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label + "："), gbc);

        gbc.gridx = gridx + 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    /**
     * 打开目录选择器（默认打开当前项目根目录或 gen 目录）
     */
    /**
     * 使用 IntelliJ 原生文件选择器选择输出路径
     */
    private void chooseOutputPath(Project project) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(
                false,  // chooseFiles
                true,   // chooseFolders
                false,  // chooseJars
                false,  // chooseJarsAsFiles
                false,  // chooseJarContents
                false   // chooseMultiple
        );
        descriptor.setTitle("选择代码生成目录");
        descriptor.setRoots(ProjectUtil.guessProjectDir(project));

        VirtualFile[] files = FileChooserFactory.getInstance()
                .createFileChooser(descriptor, project, null)
                .choose(project);

        if (files.length > 0) {
            outputPathField.setText(files[0].getPath());
        }
    }


    /**
     * 字段信息面板
     */
    private JPanel createFieldPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {
                "序号", "字段列名", "字段描述", "物理类型",
                "Java类型", "Java字段", "Go类型", "Go字段",
                "插入", "编辑", "列表", "查询", "查询方式", "必填", "显示类型", "字典类型"
        };

        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 8, 9, 10, 11, 13 -> Boolean.class;
                    default -> String.class;
                };
            }
        };

        fieldTable = new JTable(model);
        fieldTable.setRowHeight(28);
        JComboBox<QueryType> queryTypeCombo = new JComboBox<>(QueryType.values());
        fieldTable.getColumnModel().getColumn(12).setCellEditor(new DefaultCellEditor(queryTypeCombo));

        panel.add(new JScrollPane(fieldTable), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 从UI收集配置信息
     */
    private GenTable collectGenTable() {
        GenTable table = this.config == null ? new GenTable() : this.config;

        table.setTableName(tableNameField.getText());
        table.setTableComment(tableCommentField.getText());
        table.setClassName(classNameField.getText());
        table.setFunctionAuthor(authorField.getText());
        table.setModuleName(moduleField.getText());
        table.setBusinessName(businessNameField.getText());
        table.setFunctionName(functionNameField.getText());
        table.setPackageName(packageField.getText());
        table.setOverwrite(overwriteCheck.isSelected());
        table.setGenPath(outputPathField.getText());

        TemplateType tplType = (TemplateType) templateCombo.getSelectedItem();
        table.setTplCategory(tplType != null ? tplType.getValue() : TemplateType.GoAtlas.getValue());

        DefaultTableModel model = (DefaultTableModel) fieldTable.getModel();
        List<GenTableColumn> columns = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            GenTableColumn col = new GenTableColumn();
            col.setSort(Integer.parseInt(safeString(model.getValueAt(i, 0), "0")));
            col.setColumnName(safeString(model.getValueAt(i, 1)));
            col.setColumnComment(safeString(model.getValueAt(i, 2)));
            col.setColumnType(safeString(model.getValueAt(i, 3)));
            col.setJavaType(safeString(model.getValueAt(i, 4)));
            col.setJavaField(safeString(model.getValueAt(i, 5)));
            col.setGolangType(safeString(model.getValueAt(i, 6)));
            col.setGolangField(safeString(model.getValueAt(i, 7)));
            col.setIsInsert(boolToFlag(model.getValueAt(i, 8)));
            col.setIsEdit(boolToFlag(model.getValueAt(i, 9)));
            col.setIsList(boolToFlag(model.getValueAt(i, 10)));
            col.setIsQuery(boolToFlag(model.getValueAt(i, 11)));
            col.setIsRequired(boolToFlag(model.getValueAt(i, 13)));

            Object val = model.getValueAt(i, 12);
            if (val instanceof QueryType qt) {
                col.setQueryType(qt.getSymbol());
            } else {
                col.setQueryType("=");
            }

            col.setHtmlType(safeString(model.getValueAt(i, 14)));
            col.setDictType(safeString(model.getValueAt(i, 15)));
            columns.add(col);
        }
        table.setColumns(columns);
        return table;
    }

    private String safeString(Object obj) {
        return safeString(obj, "");
    }

    private String safeString(Object obj, String def) {
        return obj == null ? def : obj.toString().trim();
    }

    private String boolToFlag(Object value) {
        if (value instanceof Boolean b) return b ? "1" : "0";
        if (value == null) return "0";
        String s = value.toString().trim();
        return ("1".equals(s) || "true".equalsIgnoreCase(s)) ? "1" : "0";
    }

    /**
     * 载入已有配置（可选）
     */
    private void loadConfig(GenTable cfg) {
        if (cfg == null) return;
        tableNameField.setText(cfg.getTableName());
        tableCommentField.setText(cfg.getTableComment());
        classNameField.setText(cfg.getClassName());
        authorField.setText(cfg.getFunctionAuthor());
        moduleField.setText(cfg.getModuleName());
        businessNameField.setText(cfg.getBusinessName());
        functionNameField.setText(cfg.getFunctionName());
        packageField.setText(cfg.getPackageName());
        outputPathField.setText(cfg.getGenPath());
        overwriteCheck.setSelected(cfg.getOverwrite());
        if (cfg.getTplCategory() != null) {
            templateCombo.setSelectedItem(TemplateType.fromValue(cfg.getTplCategory()));
        }

        if (cfg.getColumns() != null && !cfg.getColumns().isEmpty()) {
            DefaultTableModel model = (DefaultTableModel) fieldTable.getModel();
            model.setRowCount(0);
            for (GenTableColumn col : cfg.getColumns()) {
                QueryType qt = QueryType.fromSymbol(col.getQueryType());
                model.addRow(new Object[]{
                        col.getSort(),
                        col.getColumnName(),
                        col.getColumnComment(),
                        col.getColumnType(),
                        col.getJavaType(),
                        col.getJavaField(),
                        col.getGolangType(),
                        col.getGolangField(),
                        "1".equals(col.getIsInsert()),
                        "1".equals(col.getIsEdit()),
                        "1".equals(col.getIsList()),
                        "1".equals(col.getIsQuery()),
                        qt,
                        "1".equals(col.getIsRequired()),
                        col.getHtmlType(),
                        col.getDictType()
                });
            }
        }
    }
}
