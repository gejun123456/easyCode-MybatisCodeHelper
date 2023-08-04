package com.bruce.plugin.actions;

import com.bruce.plugin.dict.GlobalDict;
import com.bruce.plugin.entity.TypeMapper;
import com.bruce.plugin.enums.MatchType;
import com.bruce.plugin.scratch.MyScratchUtils;
import com.bruce.plugin.tool.CurrGroupUtils;
import com.bruce.plugin.tool.StringUtils;
import com.bruce.plugin.ui.GenerateFromScratchFileUi;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.extensionResources.ExtensionsRootType;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.ui.JBUI;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.bruce.plugin.scratch.MyScratchUtils.EASYCODESUB;

/**
 * @author bruce ge 2023/4/3
 */
public class GenerateFromScratchFileAction extends AnAction {
    /**
     * 构造方法
     *
     * @param text 菜单名称
     */
    GenerateFromScratchFileAction(@Nullable String text) {
        super(text);
    }

    /**
     * 处理方法
     *
     * @param event 事件对象
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        //save files in project
        FileDocumentManager.getInstance().saveAllDocuments();

        // 校验类型映射 需要后置这个
//        if (!typeValidator(project, CacheDataUtils.getInstance().getSelectDbTable())) {
//            // 没通过不打开窗口
//            return;
//        }
        //开始处理
        // TODO: need to check if group.json file exist
        List<String> easyCodeDirectoryList = MyScratchUtils.getEasyCodeDirectoryList(project);
        boolean s = checkIfFileExist(easyCodeDirectoryList);
        if (!s) {
            //init easyCode folder for user.
            int i1 = Messages.showDialog("EasyCode Template File Not Found, Should Have EasyCode Folder with group.json File" , "EasyCode Template File Not Found" , new String[]{"Init EasyCode Folder In Scratch file" , "How to use" , "Cancel"}, 0, null);
            if(i1==0){
                String rootPath = ScratchFileService.getInstance().getRootPath(ExtensionsRootType.getInstance());
                String easyCodePath = rootPath + "/" + EASYCODESUB;
                //load zip from resource and save to user path.
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("EasyCode/EasyCode.zip" );
                //unpack the zip file and save to user path
                unzipTheFileAndSaveToPath(resourceAsStream, rootPath);
                VfsUtil.markDirtyAndRefresh(false,true,true,new File(easyCodePath));
                String easyCodeGroupFile = MyScratchUtils.getEasyCodeGroupFile(easyCodePath);
                VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(new File(easyCodeGroupFile), true);
                if(fileByIoFile!=null){
                    PsiFile file = PsiManager.getInstance(project).findFile(fileByIoFile);
                    if(file!=null){
                        CodeInsightUtil.positionCursor(project,file,file);
                    }
                }

//                try {
//                    copyFromJar("EasyCode",new File(easyCodePath).toPath());
//                } catch (URISyntaxException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }

            } else if(i1==1){
                String s1 = easyCodeDirectoryList.get(0);
                File file = new File(s1);
                if (!file.exists()) {
                    file.mkdirs();
                    MyScratchUtils.markDirtyAndRefresh(false, false, true, file);
                }
                BrowserUtil.browse("https://github.com/gejun123456/EasyCodeMybatisCodeHelperTemplates");
            }
           return;
        } else {
            new GenerateFromScratchFileUi(event.getProject()).show();
        }
    }

    //unzip the file and save to user path
    private void unzipTheFileAndSaveToPath(InputStream resourceAsStream, String easyCodePath) {
        //unzip the resourceStream
        try {
            ZipUtils.unzip(resourceAsStream, easyCodePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void copyFromJar(String source, final Path target) throws URISyntaxException, IOException {
        URI resource = getClass().getClassLoader().getResource("EasyCode").toURI();
        FileSystem fileSystem = FileSystems.newFileSystem(
                resource,
                Collections.<String, String>emptyMap()
        );


        final Path jarPath = fileSystem.getPath(source);

        Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {

            private Path currentTarget;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                currentTarget = target.resolve(jarPath.relativize(dir).toString());
                Files.createDirectories(currentTarget);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

        });
    }




    private boolean checkIfFileExist(List<String> easyCodeDirectoryList) {
        boolean s = false;
        for (String palce : easyCodeDirectoryList) {
            if (new File(palce).exists()) {
                String easyCodeGroupFile = MyScratchUtils.getEasyCodeGroupFile(palce);
                if (new File(easyCodeGroupFile).exists()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 类型校验，如果存在未知类型则引导用于去条件类型
     *
     * @param dbTable 原始表对象
     * @return 是否验证通过
     */
    private boolean typeValidator(Project project, DbTable dbTable) {
        // 处理所有列
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
        List<TypeMapper> typeMapperList = CurrGroupUtils.getCurrTypeMapperGroup().getElementList();

        // 简单的记录报错弹窗次数，避免重复报错
        Set<String> errorCount = new HashSet<>();

        FLAG:
        for (DasColumn column : columns) {
            String typeName = column.getDataType().getSpecification();
            for (TypeMapper typeMapper : typeMapperList) {
                try {
                    if (typeMapper.getMatchType() == MatchType.ORDINARY) {
                        if (typeName.equalsIgnoreCase(typeMapper.getColumnType())) {
                            continue FLAG;
                        }
                    } else {
                        // 不区分大小写的正则匹配模式
                        if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName).matches()) {
                            continue FLAG;
                        }
                    }
                } catch (PatternSyntaxException e) {
                    if (!errorCount.contains(typeMapper.getColumnType())) {
                        Messages.showWarningDialog(
                                "类型映射《" + typeMapper.getColumnType() + "》存在语法错误，请及时修正。报错信息:" + e.getMessage(),
                                GlobalDict.TITLE_INFO);
                        errorCount.add(typeMapper.getColumnType());
                    }
                }
            }
            // 没找到类型，提示用户选择输入类型
            new MainAction.Dialog(project, typeName).showAndGet();
        }
        return true;
    }

    public static class Dialog extends DialogWrapper {

        private String typeName;

        private JPanel mainPanel;

        private ComboBox<String> comboBox;

        protected Dialog(@Nullable Project project, String typeName) {
            super(project);
            this.typeName = typeName;
            this.initPanel();
        }

        private void initPanel() {
            setTitle(GlobalDict.TITLE_INFO);
            String msg = String.format("数据库类型%s，没有找到映射关系，请输入想转换的类型？", typeName);
            JLabel label = new JLabel(msg);
            this.mainPanel = new JPanel(new BorderLayout());
            this.mainPanel.setBorder(JBUI.Borders.empty(5, 10, 7, 10));
            mainPanel.add(label, BorderLayout.NORTH);
            this.comboBox = new ComboBox<>(GlobalDict.DEFAULT_JAVA_TYPE_LIST);
            this.comboBox.setEditable(true);
            this.mainPanel.add(this.comboBox, BorderLayout.CENTER);
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            return this.mainPanel;
        }

        @Override
        protected void doOKAction() {
            super.doOKAction();
            String selectedItem = (String) this.comboBox.getSelectedItem();
            if (StringUtils.isEmpty(selectedItem)) {
                return;
            }
            TypeMapper typeMapper = new TypeMapper();
            typeMapper.setMatchType(MatchType.ORDINARY);
            typeMapper.setJavaType(selectedItem);
            typeMapper.setColumnType(typeName);
            CurrGroupUtils.getCurrTypeMapperGroup().getElementList().add(typeMapper);
        }
    }

    private static class ZipUtils {
        public static void unzip(InputStream resourceAsStream, String easyCodePath) {
            try {
                ZipInputStream zipInputStream = new ZipInputStream(resourceAsStream);
                ZipEntry nextEntry = zipInputStream.getNextEntry();
                while (nextEntry != null) {
                    String name = nextEntry.getName();
                    if (nextEntry.isDirectory()) {
                        File file = new File(easyCodePath + "/" + name);
                        file.mkdirs();
                    } else {
                        File file = new File(easyCodePath + "/" + name);
                        file.getParentFile().mkdirs();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        IOUtils.copy(zipInputStream, fileOutputStream);
                        fileOutputStream.close();
                    }
                    nextEntry = zipInputStream.getNextEntry();
                }
                zipInputStream.closeEntry();
                zipInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
