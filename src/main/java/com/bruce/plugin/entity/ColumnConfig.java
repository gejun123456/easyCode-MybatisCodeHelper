package com.bruce.plugin.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列配置信息
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
@NoArgsConstructor
public class ColumnConfig {
    /**
     * 标题
     */
    private String title;
    /**
     * 类型
     */
    private ColumnConfigType type;
    /**
     * 可选值，逗号分割
     */
    private String selectValue;

    public ColumnConfig(String title, ColumnConfigType type) {
        this.title = title;
        this.type = type;
    }

    public ColumnConfig(String title, ColumnConfigType type, String selectValue) {
        this.title = title;
        this.type = type;
        this.selectValue = selectValue;
    }
}
