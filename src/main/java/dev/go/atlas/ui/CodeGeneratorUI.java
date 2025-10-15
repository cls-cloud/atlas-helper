package dev.go.atlas.ui;

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

    // 基础信息
    private final JTextField tableNameField = new JTextField();
    private final JTextField tableCommentField = new JTextField();
    private final JTextField classNameField = new JTextField();
    private final JTextField packageField = new JTextField();
    private final JTextField moduleField = new JTextField();
    private final JTextField businessNameField = new JTextField();
    private final JTextField functionNameField = new JTextField();
    private final JTextField authorField = new JTextField();

    // 字段信息表格
    private JTable fieldTable;

    // 其他选项
    private final JCheckBox overwriteCheck = new JCheckBox("覆盖已有文件", true);

    // 模板下拉框
    private JComboBox<TemplateType> templateCombo;

    private GenTable config;

    public CodeGeneratorUI() {
        initContainer();
    }

    public CodeGeneratorUI(GenTable tableInfo) {
        initContainer();
        this.config = tableInfo == null ? new GenTable() : tableInfo;
        loadConfig(config);
    }

    public GenTable getGenTable() {
        return collectGenTable();
    }

    private void initContainer() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("基础信息", createBaseInfoPanel());
        tabbedPane.addTab("字段信息", createFieldPanel());
        tabbedPane.addTab("生成信息", createGenerateInfoPanel());

        JButton genButton = new JButton("导出 GenTable 对象");
        genButton.addActionListener(e -> {
            GenTable table = collectGenTable();
            System.out.println(table);
        });

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        southPanel.add(genButton, BorderLayout.CENTER);

        container.add(tabbedPane, BorderLayout.CENTER);
        container.add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * 基础信息 Tab
     */
    private JPanel createBaseInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"表名称", "表描述", "实体类名", "作者"};
        JTextField[] fields = {tableNameField, tableCommentField, classNameField, authorField};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(new JLabel(labels[i] + "："), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(fields[i], gbc);
        }
        return panel;
    }

    /**
     * 字段信息 Tab
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
                // Boolean 列
                return switch (columnIndex) {
                    case 8, 9, 10, 11, 13 -> Boolean.class;
                    default -> String.class;
                };
            }
        };

        fieldTable = new JTable(model);
        fieldTable.setRowHeight(28);

        // 查询方式下拉框
        JComboBox<QueryType> queryTypeCombo = new JComboBox<>(QueryType.values());
        fieldTable.getColumnModel().getColumn(12).setCellEditor(new DefaultCellEditor(queryTypeCombo));

        panel.add(new JScrollPane(fieldTable), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 生成信息 Tab
     */
    private JPanel createGenerateInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        templateCombo = new JComboBox<>(TemplateType.values());
        templateCombo.setSelectedItem(TemplateType.GoAtlas);

        String[] labels = {"生成模板", "生成包路径", "生成模块名", "生成业务名", "生成功能名"};
        JComponent[] fields = {templateCombo, packageField, moduleField, businessNameField, functionNameField};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(new JLabel(labels[i] + "："), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(fields[i], gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        optionPanel.add(overwriteCheck);
        panel.add(optionPanel, gbc);

        return panel;
    }

    private void loadConfig(GenTable cfg) {
        if (cfg == null) return;

        tableNameField.setText(cfg.getTableName());
        tableCommentField.setText(cfg.getTableComment());
        classNameField.setText(cfg.getClassName());
        moduleField.setText(cfg.getModuleName());
        authorField.setText(cfg.getFunctionAuthor());
        businessNameField.setText(cfg.getBusinessName());
        functionNameField.setText(cfg.getFunctionName());
        packageField.setText(cfg.getPackageName());
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

    private GenTable collectGenTable() {
        GenTable table = this.config;
        table.setTableName(tableNameField.getText());
        table.setTableComment(tableCommentField.getText());
        table.setClassName(classNameField.getText());
        table.setFunctionAuthor(authorField.getText());
        table.setModuleName(moduleField.getText());
        table.setBusinessName(businessNameField.getText());
        table.setFunctionName(functionNameField.getText());
        table.setPackageName(packageField.getText());
        table.setOverwrite(overwriteCheck.isSelected());

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
        if (value instanceof Boolean b) {
            return b ? "1" : "0";
        }
        if (value == null) return "0";
        String s = value.toString().trim();
        return ("1".equals(s) || "true".equalsIgnoreCase(s)) ? "1" : "0";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CodeGeneratorUI ui = new CodeGeneratorUI();
            JFrame frame = new JFrame("代码生成配置");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 600);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(ui.getContainer());
            frame.setVisible(true);
        });
    }
}
