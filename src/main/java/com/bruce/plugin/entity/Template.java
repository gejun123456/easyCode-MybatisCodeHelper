package com.bruce.plugin.entity;

import com.bruce.plugin.ui.base.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板信息类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Template implements Item {
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板代码
     */
    private String code;
}
