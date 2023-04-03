package com.bruce.plugin.dto;

/**
 * @author bruce ge 2023/3/31
 */
public class GroupInfo {
    private String groupName = "";

    private String templateName = "";

    private String globalConfigName = "";

    private String columnConfigName = "";

    private String typeMapperName = "";

    private String tableNameRegex = ".*";

    private String schemaNameRegex = ".*";

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getGlobalConfigName() {
        return globalConfigName;
    }

    public void setGlobalConfigName(String globalConfigName) {
        this.globalConfigName = globalConfigName;
    }

    public String getColumnConfigName() {
        return columnConfigName;
    }

    public void setColumnConfigName(String columnConfigName) {
        this.columnConfigName = columnConfigName;
    }

    public String getTypeMapperName() {
        return typeMapperName;
    }

    public void setTypeMapperName(String typeMapperName) {
        this.typeMapperName = typeMapperName;
    }

    public String getTableNameRegex() {
        return tableNameRegex;
    }

    public void setTableNameRegex(String tableNameRegex) {
        this.tableNameRegex = tableNameRegex;
    }

    public String getSchemaNameRegex() {
        return schemaNameRegex;
    }

    public void setSchemaNameRegex(String schemaNameRegex) {
        this.schemaNameRegex = schemaNameRegex;
    }

    @Override
    public String toString() {
        return groupName;
    }
}
