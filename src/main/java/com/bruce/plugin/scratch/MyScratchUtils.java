package com.bruce.plugin.scratch;

import com.bruce.plugin.dto.GroupInfo;
import com.bruce.plugin.dto.SettingsStorageDTO;
import com.bruce.plugin.entity.*;
import com.bruce.plugin.enums.ColumnConfigType;
import com.bruce.plugin.service.SettingsStorageService;
import com.bruce.plugin.tool.CurrGroupUtils;
import com.bruce.plugin.tool.JSON;
import com.bruce.plugin.tool.ProjectUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.ide.extensionResources.ExtensionsRootType;
import com.intellij.ide.scratch.RootType;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import kotlin.jvm.JvmStatic;
import kotlin.text.Charsets;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class MyScratchUtils {
    @NotNull
    public static final MyScratchUtils INSTANCE;
    public static final String TEMPLATE_NAME = "templateName";
    public static final String GLOBALCONFIGNAME = "globalConfigName";
    public static final String COLUMNCONFIGNAME = "columnConfigName";
    public static final String TYPEMAPPERCONFIGNAME = "typeMapperName";
    public static final String GLOBAL_CONFIG = "GlobalConfig";
    public static final String COLUMN_CONFIG = "ColumnConfig";
    public static final String TYPE_MAPPER_CONFIG = "TypeMapperConfig";
    public static final String TEMPLATESFOLDERNAME = "Templates";
    public static String EASYCODESUB = "EasyCode";


    @JvmStatic
    @NotNull
    public static final String handleImportFromJson() {
        String json = (String) CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
        SettingsStorageDTO parse = (SettingsStorageDTO) JSON.parse(json, SettingsStorageDTO.class);
        String var10000 = ScratchFileService.getInstance().getRootPath((RootType) ExtensionsRootType.getInstance());
        String rootPath = var10000;
        String s = TEMPLATESFOLDERNAME;
        Map templateGroupMap = parse.getTemplateGroupMap();
        MyScratchUtils var5 = INSTANCE;
        //get sub directory
        String easycodesub = EASYCODESUB;
        String s1 = rootPath + "/" + easycodesub;
        File file = new File(FileUtil.toSystemDependentName(s1));
        Integer count = 1;
        String qqq = s1;
        while(file.exists()){
            qqq = s1+"_"+count.toString();
            file = new File(FileUtil.toSystemDependentName(qqq));
            count++;
        }
        s1 = qqq;
        var5.writeFilesToScratchFile(s1, s, templateGroupMap);
        Map<String, GlobalConfigGroup> globalConfigGroupMap = parse.getGlobalConfigGroupMap();
        var5.writeFilesToScratchFile(s1, GLOBAL_CONFIG, globalConfigGroupMap);

        Map<String, ColumnConfigGroup> columnConfigGroupMap = parse.getColumnConfigGroupMap();
        var5.writeFilesToScratchFile(s1, COLUMN_CONFIG, columnConfigGroupMap);

        Map<String, TypeMapperGroup> typeMapperGroupMap = parse.getTypeMapperGroupMap();
        var5.writeFilesToScratchFile(s1, TYPE_MAPPER_CONFIG, typeMapperGroupMap);
        Project currProject = ProjectUtils.getCurrProject();
        VfsUtil.markDirtyAndRefresh(false,true,true,new File(FileUtil.toSystemDependentName(rootPath + "/EasyCode")));
        return "success";
    }

    @NotNull
    public static String getEasyCodeDirectory() {
        String rootPath = ScratchFileService.getInstance().getRootPath(ExtensionsRootType.getInstance());
        String easyCodePath = rootPath + "/" + EASYCODESUB;
        return easyCodePath;
    }


    @NotNull
    public static String getEasyCodeSubDirectory(String subDirectory) {
        String rootPath = getEasyCodeDirectory();
        String easyCodePath = rootPath+"/"+subDirectory;
        return easyCodePath;
    }


    @NotNull
    public static String getEasyCodeTemplateDirectory() {
        String rootPath = getEasyCodeDirectory();
        String easyCodePath = rootPath +"/"+MyScratchUtils.TEMPLATESFOLDERNAME;
        return easyCodePath;
    }


    @NotNull
    public static String getEasyCodeGroupFile() {
        String rootPath = getEasyCodeDirectory();
        String easyCodePath = rootPath +"/group.json";
        return easyCodePath;
    }

    public static String readFileToString(File file3) {
        try {
            FileInputStream input = new FileInputStream(file3);
            String s = IOUtils.toString(input, Charsets.UTF_8);
            input.close();
            return s;
        } catch (IOException e) {
            return "";
        }
    }

    @Nullable
    public static List<GroupInfo> getAllGroupsFromFile(String easyCodeGroupFile) {
        String s = readFileToString(new File(easyCodeGroupFile));
        List<GroupInfo> groupInfos = new Gson().fromJson(s, new TypeToken<List<GroupInfo>>() {
        }.getType());
        return groupInfos;
    }

    public static boolean setDefaultForGroups(String groupName, @NotNull Project project) {
        String easyCodeGroupFile = getEasyCodeGroupFile();
        List<GroupInfo> groupInfos = getAllGroupsFromFile(easyCodeGroupFile);
        GroupInfo selectedItem1 = null;
        for (GroupInfo groupInfo : groupInfos) {
            if (groupInfo.getGroupName().equals(groupName)) {
                selectedItem1 = groupInfo;
                break;
            }
        }
        if(selectedItem1==null){
            Messages.showErrorDialog("group info is not found in file " + easyCodeGroupFile, "error");
            return true;
        }

        String templateName = selectedItem1.getTemplateName();
        String globalConfigName = selectedItem1.getGlobalConfigName();
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        Map<String, GlobalConfigGroup> globalConfigGroupMap = settingsStorage.getGlobalConfigGroupMap();
        GlobalConfigGroup currGlobalConfigGroup = CurrGroupUtils.getCurrGlobalConfigGroup();
        GlobalConfigGroup value = new GlobalConfigGroup();
        String tempscratchName = "TEMPSCRATCH";
        globalConfigGroupMap.put(tempscratchName, value);
        value.setName(tempscratchName);
        //serach in files.
        String globalConfigDirectory = getEasyCodeSubDirectory(GLOBAL_CONFIG + "/" + selectedItem1.getGlobalConfigName());
        ArrayList<GlobalConfig> myArray = Lists.newArrayList();
        value.setElementList(myArray);
        File file2 = new File(globalConfigDirectory);
        if (!file2.exists()) {
            Messages.showErrorDialog(project, "Global config group not exist", "error");
            return true;
        }
        File[] files = file2.listFiles();
        for (File file3 : files) {
            GlobalConfig globalConfig = new GlobalConfig();
            globalConfig.setName(file3.getName());
            globalConfig.setValue(readFileToString(file3));
            myArray.add(globalConfig);
        }
        settingsStorage.setCurrGlobalConfigGroupName(tempscratchName);
        //
        String columnConfigFileName = getEasyCodeSubDirectory(COLUMN_CONFIG + "/" + selectedItem1.getColumnConfigName());
        String columnConfig = readFileToString(new File(columnConfigFileName));
        Map<String, ColumnConfigGroup> columnConfigGroupMap = settingsStorage.getColumnConfigGroupMap();
        ColumnConfigGroup value1 = new ColumnConfigGroup();
        value1.setName(tempscratchName);
        List<ColumnConfig> o = new Gson().fromJson(columnConfig, new TypeToken<List<ColumnConfig>>() {
        }.getType());
        value1.setElementList(o);
        columnConfigGroupMap.put(tempscratchName, value1);
        settingsStorage.setCurrColumnConfigGroupName(tempscratchName);

        String typeMapperFileName = getEasyCodeSubDirectory(TYPE_MAPPER_CONFIG + "/" + selectedItem1.getTypeMapperName());
        String typeMapper = readFileToString(new File(typeMapperFileName));
        Map<String, TypeMapperGroup> typeMapperGroupMap = settingsStorage.getTypeMapperGroupMap();
        TypeMapperGroup value2 = new TypeMapperGroup();
        value2.setName(tempscratchName);
        List<TypeMapper> o1 = new Gson().fromJson(typeMapper, new TypeToken<List<TypeMapper>>() {
        }.getType());
        value2.setElementList(o1);
        typeMapperGroupMap.put(tempscratchName, value2);
        settingsStorage.setCurrTypeMapperGroupName(tempscratchName);
        return false;
    }

    private final void writeFilesToScratchFile(String s1, String s, Map templateGroupMap) {

        final File file1 = new File(FileUtil.toSystemDependentName(s1 + "/" + s));
        templateGroupMap.forEach((BiConsumer) (new BiConsumer() {
            // $FF: synthetic method
            // $FF: bridge method
            public void accept(Object var1, Object var2) {
                try {
                    this.accept((String) var1, (AbstractGroup) var2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public final void accept(@NotNull String t, @NotNull AbstractGroup u) throws IOException {

                String name = u.getName();
                List elementList = u.getElementList();
                if (u instanceof TypeMapperGroup) {
                    TypeMapperGroup typeMapper = (TypeMapperGroup) u;
                    String name1 = typeMapper.getName();
                    List<TypeMapper> elementList1 = typeMapper.getElementList();
                    File file2 = new File(file1, name1);
                    StringBuilder builder = new StringBuilder();
//                    for (TypeMapper mapper : elementList1) {
//                        MatchType matchType = mapper.getMatchType();
//                        String javaType = mapper.getJavaType();
//                        String columnType = mapper.getColumnType();
//
//                    }
                    String s1 = new Gson().toJson(elementList1);
                    writeContentTofile(file1, name1+".json", s1);
                } else if (u instanceof ColumnConfigGroup) {
                    ColumnConfigGroup columnConfigGroup = (ColumnConfigGroup) u;
                    String name1 = columnConfigGroup.getName();
                    List<ColumnConfig> elementList1 = columnConfigGroup.getElementList();
                    File file2 = new File(file1, name1);
                    StringBuilder builder = new StringBuilder();
                    for (ColumnConfig mapper : elementList1) {
                        ColumnConfigType type = mapper.getType();
                        String title = mapper.getTitle();
                        String selectValue = mapper.getSelectValue();
                        builder.append("type=" + type.toString() + " title=" + title + " " + "selectValue=" + selectValue + "\n");
                    }
                    String s1 = new Gson().toJson(elementList1);
                    writeContentTofile(file1, name1+".json", s1);
                } else {
                    File file = new File(file1, t);
                    Iterator var7 = elementList.iterator();
                    while (var7.hasNext()) {

                        Object next = var7.next();
                        if (next instanceof Template) {
                            Template template = (Template) next;
                            String name1 = template.getName();
                            String code1 = template.getCode();

                            writeContentTofile(file, name1, code1);
                        } else if (next instanceof GlobalConfig) {
                            GlobalConfig template = (GlobalConfig) next;
                            String name1 = template.getName();
                            String code1 = template.getValue();
                            writeContentTofile(file, name1, code1);
                        }
                    }
                }
            }
        }));
    }

    private void writeContentTofile(File file, String name1, String code1) throws IOException {
        File file2 = new File(file, name1);
        if (!file2.getParentFile().exists()) {
            file2.getParentFile().mkdirs();
        }
        String code = code1;
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        IOUtils.write(code, (OutputStream) fileOutputStream, Charsets.UTF_8);
        fileOutputStream.close();
    }

    private MyScratchUtils() {
    }

    static {
        MyScratchUtils var0 = new MyScratchUtils();
        INSTANCE = var0;
    }
}

