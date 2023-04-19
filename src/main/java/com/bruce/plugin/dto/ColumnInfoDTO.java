package com.bruce.plugin.dto;

import com.bruce.plugin.entity.TypeMapperGroup;
import com.intellij.database.model.DasColumn;
import com.intellij.psi.PsiField;
import com.bruce.plugin.entity.TypeMapper;
import com.bruce.plugin.enums.MatchType;
import com.bruce.plugin.tool.CurrGroupUtils;
import com.bruce.plugin.tool.DocCommentUtils;
import com.bruce.plugin.tool.NameUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 列信息传输对象
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 17:29
 */
@Data
@NoArgsConstructor
public class ColumnInfoDTO {

    public ColumnInfoDTO(PsiField field) {
        this.name = field.getName();
        this.comment = DocCommentUtils.getComment(field.getDocComment());
        this.type = field.getType().getCanonicalText();
        this.custom = false;
        this.ext = "{}";
    }

    public ColumnInfoDTO(DasColumn column) {
        this.name = NameUtils.getInstance().getJavaName(column.getName());
        this.comment = column.getComment();
        this.type = getJavaType(column.getDataType().toString());
        this.custom = false;
        this.ext = "{}";
    }

    private String getJavaType(String dbType) {
        TypeMapperGroup currTypeMapperGroup = CurrGroupUtils.getCurrTypeMapperGroup();
        if(currTypeMapperGroup==null){
            return "java.lang.Object";
        }
        for (TypeMapper typeMapper : currTypeMapperGroup.getElementList()) {
            if (typeMapper.getMatchType() == MatchType.ORDINARY) {
                if (dbType.equalsIgnoreCase(typeMapper.getColumnType())) {
                    return typeMapper.getJavaType();
                }
            } else {
                // 不区分大小写的正则匹配模式
                if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(dbType).matches()) {
                    return typeMapper.getJavaType();
                }
            }
        }
        return "java.lang.Object";
    }

    /**
     * 名称
     */
    private String name;
    /**
     * 注释
     */
    private String comment;
    /**
     * 全类型
     */
    private String type;
    /**
     * 标记是否为自定义附加列
     */
    private Boolean custom;
    /**
     * 扩展数据(JSON字符串)
     */
    private String ext;
}
