package com.bruce.plugin.scratch;

import com.bruce.plugin.dict.GlobalDict;
import com.bruce.plugin.dto.GroupInfo;
import com.bruce.plugin.entity.*;
import com.bruce.plugin.service.CodeGenerateService;
import com.bruce.plugin.service.TableInfoSettingsService;
import com.bruce.plugin.tool.*;
import com.bruce.plugin.ui.base.EditorSettingsInit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.database.model.DasNamespace;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.JBIterable;
import kotlin.text.Charsets;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * @author bruce ge 2023/3/28
 */
public class EasyCodeNotificationPanel extends EditorNotificationPanel {
    public EasyCodeNotificationPanel(VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
        super();
        setText("easy code files");

        String path = virtualFile.getPath();
        String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeTemplateDirectory();
        if (path.startsWith(easyCodeTemplateDirectory)) {
            createActionLabel("easyCode", new Runnable() {
                @Override
                public void run() {
                    System.out.println("easy code");
                }
            });
            JLabel groupLabel = new JLabel("group:");
            myLinksPanel.add(groupLabel);
            ComboBox<GroupInfo> groupComboBox = new ComboBox<>();
            loadGroups(project,groupComboBox,false);
            //read group info from text.
            myLinksPanel.add(groupComboBox);

            JButton addGroupButton = new JButton("add group");
            myLinksPanel.add(addGroupButton);

            addGroupButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //go to the file.
                    //add group info to the file.
                    String easyCodeDirectory = MyScratchUtils.getEasyCodeDirectory();
                    String s = easyCodeDirectory + "/" + "group.json";
                    File file = new File(s);
                    if (file.exists()) {
                        VirtualFile file1 = VfsUtil.findFile(file.toPath(), true);
                        PsiFile file2 = PsiManager.getInstance(project).findFile(file1);
                        if (file2 != null) {
                            //this should not be null.
                            CodeInsightUtil.positionCursor(project, file2, file2);
                        }
                    } else {
                        File file1 = new File(s);
                        GroupInfo groupInfo1 = new GroupInfo();
                        List<GroupInfo> groupInfos = Lists.newArrayList();
                        groupInfos.add(groupInfo1);
                        String s1 = new Gson().toJson(groupInfos);
                        try {
                            FileOutputStream output = new FileOutputStream(file1);
                            IOUtils.write(s1, output, Charsets.UTF_8);
                            output.close();
                            VirtualFile file2 = VfsUtil.findFile(file1.toPath(), true);
                            PsiFile file3 = PsiManager.getInstance(project).findFile(file2);
                            if (file3 != null) {
                                //this should not be null.
                                CodeInsightUtil.positionCursor(project, file3, file3);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            });

            JButton refreshGroupButton = new JButton("refresh group info");

            refreshGroupButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadGroups(project, groupComboBox,true);
                }
            });

            myLinksPanel.add(refreshGroupButton);

            JLabel label = new JLabel("choose table:");
            myLinksPanel.add(label);
            ComboBox<String> tableCombox = new ComboBox<>();

            Map<String, DasTable> allTables = findAllTablesInProject(project);
            tableCombox.removeAllItems();
            for (String tableName : getAllTableNameBySort(allTables)) {
                tableCombox.addItem(tableName);
            }
            DbTable selectDbTable = CacheDataUtils.getInstance().getSelectDbTable();
            if(selectDbTable!=null&&allTables.containsKey(selectDbTable.toString())){
                tableCombox.setSelectedItem(selectDbTable.toString());
            }
            myLinksPanel.add(tableCombox);

            JButton debugButton = new JButton("DEBUG");
            debugButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object selectedItem = groupComboBox.getSelectedItem();
                    if (selectedItem == null) {
                        Messages.showErrorDialog(project, "group is not choosed", "error");
                        return;
                    }
                    FileDocumentManager.getInstance().saveAllDocuments();
                    VirtualFile file = fileEditor.getFile();
                    PsiFile file1 = PsiManager.getInstance(project).findFile(file);
                    String text = file1.getText();
                    GroupInfo selected = (GroupInfo) selectedItem;
                    //should get latest group for it.
                    String groupName = selected.getGroupName();
                    if (MyScratchUtils.setDefaultForGroups(groupName, project)) return;
                    runDebug(tableCombox, text,project,file1.getName());
                    //set those two for it.
                }
            });
            myLinksPanel.add(debugButton);
        } else if (path.equals(MyScratchUtils.getEasyCodeGroupFile())) {
            createActionLabel("group example", new Runnable() {
                @Override
                public void run() {
                    GroupInfo groupInfo = new GroupInfo();
                    groupInfo.setGroupName("TestGroup");
                    groupInfo.setTemplateName("Default");
                    groupInfo.setGlobalConfigName("Default");
                    groupInfo.setColumnConfigName("Default");
                    groupInfo.setTypeMapperName("Default");
                    groupInfo.setSchemaNameRegex(".*");
                    groupInfo.setTableNameRegex(".*");
                    String code = new Gson().toJson(groupInfo);
                    // 创建编辑框
                    EditorFactory editorFactory = EditorFactory.getInstance();
                    Document document = editorFactory.createDocument(code);
                    // 标识为模板，让velocity跳过语法校验
                    document.putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, FileTemplateManager.getInstance(project).getDefaultProperties());
                    Editor editor = editorFactory.createViewer(document, ProjectUtils.getCurrProject());
                    // 配置编辑框
                    EditorSettingsInit.init(editor);
                    ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(ProjectUtils.getCurrProject(), "xx.json"));
                    // 构建dialog
                    DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
                    dialogBuilder.setTitle("Group Info Json");
                    JComponent component = editor.getComponent();
                    component.setPreferredSize(new Dimension(800, 600));
                    dialogBuilder.setCenterPanel(component);
                    dialogBuilder.addCloseButton();
                    dialogBuilder.addDisposable(() -> {
                        //释放掉编辑框
                        editorFactory.releaseEditor(editor);
                        dialogBuilder.dispose();
                    });
                    dialogBuilder.show();
                }
            });
        }

        //get project button.
        //弄一个combox 来debug下

    }

    private void loadGroups(@NotNull Project project, ComboBox<GroupInfo> groupComboBox,boolean showErorr) {
        String easyCodeGroupFile = MyScratchUtils.getEasyCodeGroupFile();
        File file = new File(easyCodeGroupFile);
        if (!file.exists()) {
            if(showErorr) {
                Messages.showErrorDialog(project, "group.json not exist", "file not found");
            }
            return;
        }
        try {
            FileInputStream input = new FileInputStream(file);
            String s = IOUtils.toString(input, Charsets.UTF_8);
            input.close();
            List<GroupInfo> o = new Gson().fromJson(s, new TypeToken<List<GroupInfo>>() {
            }.getType());
            if (o.isEmpty()) {
                if(showErorr) {
                    Messages.showErrorDialog(project, "No group exist in file group.json", "error");
                }
                return;
            }
            groupComboBox.removeAllItems();
            for (GroupInfo groupInfo : o) {
                groupComboBox.addItem(groupInfo);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @NotNull
    private Map<String, DasTable> findAllTablesInProject(Project project) {
        List<DbDataSource> dataSources = DbPsiFacade.getInstance(project).getDataSources();
        Map<String, DasTable> allTables = new HashMap<>(16);
        for (DbDataSource dataSource : dataSources) {
            for (DasTable table : getTables(dataSource)) {
                allTables.put(table.toString(), table);
            }
        }
        return allTables;
    }

    private void runDebug(ComboBox comboBox, String code, Project project
            , String fileName) {
        // 获取选中的表
        String name = (String) comboBox.getSelectedItem();
        Map<String, DasTable> allTables = findAllTablesInProject(project);
        DasTable dasTable = allTables.get(name);
        if (dasTable == null) {
            return;
        }
        DbTable dbTable = null;
        if (dasTable instanceof DbTable) {
            // 针对2017.2版本做兼容
            dbTable = (DbTable) dasTable;
        } else {
            Method method = ReflectionUtil.getMethod(DbPsiFacade.class, "findElement", DasObject.class);
            if (method == null) {
                Messages.showWarningDialog("findElement method not found", GlobalDict.TITLE_INFO);
                return;
            }
            try {
                // 针对2017.2以上版本做兼容
                dbTable = (DbTable) method.invoke(DbPsiFacade.getInstance(project), dasTable);
            } catch (IllegalAccessException | InvocationTargetException e1) {
                ExceptionUtil.rethrow(e1);
            }
        }
        CacheDataUtils.getInstance().setSelectDbTable(dbTable);
        // 获取表信息
        TableInfo tableInfo = TableInfoSettingsService.getInstance().getTableInfo(dbTable);
        // 为未配置的表设置一个默认包名
        if (tableInfo.getSavePackageName() == null) {
            tableInfo.setSavePackageName("com.companyname.modulename");
        }
        // 生成代码
        VelocityResult result = CodeGenerateService.getInstance(ProjectUtils.getCurrProject()).generateDebug(new Template("temp", code), tableInfo);

        if (result.isHasError()) {
            DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
            dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BorderLayout());
            JBLabel comp = new JBLabel(result.getCode());
            centerPanel.add(comp, BorderLayout.NORTH);
            dialogBuilder.setCenterPanel(centerPanel);
            String finalTemplate = result.getFinalTemplate();
            // 创建编辑框
            EditorFactory editorFactory = EditorFactory.getInstance();
            Document document = editorFactory.createDocument(finalTemplate);
            // 标识为模板，让velocity跳过语法校验
            document.putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, FileTemplateManager.getInstance(ProjectUtils.getCurrProject()).getDefaultProperties());
            Editor editor = editorFactory.createViewer(document, ProjectUtils.getCurrProject());
            // 配置编辑框
            EditorSettingsInit.init(editor);
            ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(ProjectUtils.getCurrProject(), fileName));
            JComponent component = editor.getComponent();
            component.setPreferredSize(new Dimension(800, 600));
            centerPanel.add(component, BorderLayout.CENTER);
            dialogBuilder.addCloseButton();
            dialogBuilder.addDisposable(() -> {
                //释放掉编辑框
                editorFactory.releaseEditor(editor);
                dialogBuilder.dispose();
            });
            dialogBuilder.show();
        } else {
            // 创建编辑框
            EditorFactory editorFactory = EditorFactory.getInstance();
            Document document = editorFactory.createDocument(result.getCode());
            // 标识为模板，让velocity跳过语法校验
            document.putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, FileTemplateManager.getInstance(ProjectUtils.getCurrProject()).getDefaultProperties());
            Editor editor = editorFactory.createViewer(document, ProjectUtils.getCurrProject());
            // 配置编辑框
            EditorSettingsInit.init(editor);
            ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(ProjectUtils.getCurrProject(), fileName));
            // 构建dialog
            DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
            dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
            JComponent component = editor.getComponent();
            component.setPreferredSize(new Dimension(800, 600));
            dialogBuilder.setCenterPanel(component);
            dialogBuilder.addCloseButton();
            dialogBuilder.addDisposable(() -> {
                //释放掉编辑框
                editorFactory.releaseEditor(editor);
                dialogBuilder.dispose();
            });
            dialogBuilder.show();
        }
    }

    private JBIterable<DasTable> getTables(DbDataSource dataSource) {
        return dataSource.getModel().traverser().expandAndSkip(Conditions.instanceOf(DasNamespace.class)).filter(DasTable.class);
    }

    private List<String> getAllTableNameBySort(Map<String, DasTable> allTables) {
        if (CollectionUtil.isEmpty(allTables)) {
            return Collections.emptyList();
        }
        // 表排前面，视图排后面
        List<String> tableList = new ArrayList<>();
        List<String> viewList = new ArrayList<>();
        for (String name : allTables.keySet()) {
            if (name.endsWith("table")) {
                tableList.add(name);
            } else {
                viewList.add(name);
            }
        }
        // 排序后进行拼接
        Collections.sort(tableList);
        Collections.sort(viewList);
        tableList.addAll(viewList);
        return tableList;
    }

}
