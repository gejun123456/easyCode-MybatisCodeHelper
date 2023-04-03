package com.bruce.plugin.ui.component;

import com.bruce.plugin.dto.GroupInfo;
import com.bruce.plugin.entity.Template;
import com.bruce.plugin.entity.TemplateGroup;
import com.bruce.plugin.scratch.MyScratchUtils;
import com.bruce.plugin.service.SettingsStorageService;
import com.bruce.plugin.tool.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bruce ge 2023/4/3
 */
public class GroupSelectComponent {
    @Getter
    private JPanel mainPanel;

    /**
     * 分组
     */
    private ComboBox<String> groupComboBox;

    /**
     * 选中所有复选框
     */
    private JBCheckBox allCheckbox;

    /**
     * 所有复选框
     */
    private List<JBCheckBox> checkBoxList;

    /**
     * 模板面板
     */
    private JPanel templatePanel;

    public GroupSelectComponent() {
        this.init();
    }

    private void init() {
        this.mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        this.groupComboBox = new ComboBox<>();
        this.groupComboBox.setSwingPopup(false);
        this.groupComboBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String groupName = (String) groupComboBox.getSelectedItem();
                if (StringUtils.isEmpty(groupName)) {
                    return;
                }
                refreshTemplatePanel(groupName);
            }
        });
        this.allCheckbox = new JBCheckBox("All");
        this.allCheckbox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBoxList == null) {
                    return;
                }
                for (JBCheckBox checkBox : checkBoxList) {
                    checkBox.setSelected(allCheckbox.isSelected());
                }
            }
        });
        topPanel.add(this.groupComboBox, BorderLayout.WEST);
        topPanel.add(this.allCheckbox, BorderLayout.EAST);
        this.mainPanel.add(topPanel, BorderLayout.NORTH);
        this.templatePanel = new JPanel(new GridLayout(-1, 2));
        this.mainPanel.add(templatePanel, BorderLayout.CENTER);
        this.refreshData();
    }

    private void refreshData() {
        String easyCodeGroupFile = MyScratchUtils.getEasyCodeGroupFile();
        List<GroupInfo> allGroupsFromFile = MyScratchUtils.getAllGroupsFromFile(easyCodeGroupFile);
        //get all group info from string.
        this.groupComboBox.removeAllItems();
        for (GroupInfo groupName : allGroupsFromFile) {
            this.groupComboBox.addItem(groupName.getGroupName());
        }
    }

    private void refreshTemplatePanel(String groupName) {
        this.allCheckbox.setSelected(false);
        this.templatePanel.removeAll();
        this.checkBoxList = new ArrayList<>();
        //get template from group.
        List<File> templateFileList = getGroupTemlateFileListFromGroupName(groupName);
        for (File file1 : templateFileList) {
            JBCheckBox checkBox = new JBCheckBox(file1.getName());
            this.checkBoxList.add(checkBox);
            this.templatePanel.add(checkBox);
        }
        this.mainPanel.updateUI();
    }

    @NotNull
    private List<File> getGroupTemlateFileListFromGroupName(String groupName) {
        String templateGroupFromGroupName = getTemplateGroupFromGroupName(groupName);
        String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeTemplateDirectory();
        List<File> templateFileList = Lists.newArrayList();
        File file = new File(easyCodeTemplateDirectory);
        for (File listFile : file.listFiles()) {
            if (listFile.getName().equals(templateGroupFromGroupName)) {
                for (File file1 : listFile.listFiles()) {
                    templateFileList.add(file1);
                }
                break;
            }
        }
        return templateFileList;
    }

    public String getTemplateGroupFromGroupName(String groupName) {
        String easyCodeGroupFile = MyScratchUtils.getEasyCodeGroupFile();
        List<GroupInfo> allGroupsFromFile = MyScratchUtils.getAllGroupsFromFile(easyCodeGroupFile);
        String templateGroup = null;
        for (GroupInfo groupInfo : allGroupsFromFile) {
            if (groupInfo.getGroupName().equals(groupName)) {
                templateGroup = groupInfo.getTemplateName();
            }
        }
        return templateGroup;
    }

    public String getselectedGroupName() {
        return (String) this.groupComboBox.getSelectedItem();
    }

    public void setSelectedGroupName(String groupName) {
        this.groupComboBox.setSelectedItem(groupName);
    }

    public String getSelectedGroup(){
        return (String) this.groupComboBox.getSelectedItem();
    }

    public List<Template> getAllSelectedTemplate() {
        String groupName = (String) this.groupComboBox.getSelectedItem();
        if (StringUtils.isEmpty(groupName)) {
            return Collections.emptyList();
        }
        List<File> groupTemlateFileListFromGroupName = getGroupTemlateFileListFromGroupName(groupName);
        Map<String,File> map = Maps.newHashMap();
        for (File file : groupTemlateFileListFromGroupName) {
            map.put(file.getName(),file);
        }
        List<Template> result = new ArrayList<>();
        for (JBCheckBox checkBox : this.checkBoxList) {
            if (checkBox.isSelected()) {
                File template = map.get(checkBox.getText());
                if (template != null) {
                    Template template1 = new Template();
                    template1.setName(template.getName());
                    template1.setCode(MyScratchUtils.readFileToString(template));
                    result.add(template1);
                }
            }
        }
        return result;
    }
}
