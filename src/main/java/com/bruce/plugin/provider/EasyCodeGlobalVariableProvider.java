package com.bruce.plugin.provider;

import com.intellij.psi.CommonClassNames;
import com.intellij.velocity.VtlGlobalVariableProvider;
import com.intellij.velocity.psi.VtlLightVariable;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.files.VtlFile;
import com.bruce.plugin.ui.base.TemplateEditor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * EasyCode 专用Velocity全局变量
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/8/14 11:07
 */
public class EasyCodeGlobalVariableProvider extends VtlGlobalVariableProvider {

    @NotNull
    @Override
    public Collection<VtlVariable> getGlobalVariables(@NotNull VtlFile file) {
        // 非EasyCode模板，不提供支持。
        if (!Objects.equals(file.getName(), TemplateEditor.EASY_CODE_TEMPLATE)) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        final List<VtlVariable> result = new ArrayList<>();
        result.add(new VtlLightVariable("author", file, CommonClassNames.JAVA_LANG_STRING));
        result.add(new VtlLightVariable("encode", file, CommonClassNames.JAVA_LANG_STRING));
        result.add(new VtlLightVariable("packageName", file, CommonClassNames.JAVA_LANG_STRING));
        result.add(new VtlLightVariable("modulePath", file, CommonClassNames.JAVA_LANG_STRING));
        result.add(new VtlLightVariable("importList", file, "java.util.Set<java.lang.String>"));
        result.add(new VtlLightVariable("callback", file, "com.bruce.plugin.entity.Callback"));
        result.add(new VtlLightVariable("tool", file, "com.bruce.plugin.tool.GlobalTool"));
        result.add(new VtlLightVariable("time", file, "com.bruce.plugin.tool.TimeUtils"));
        result.add(new VtlLightVariable("tableInfo", file, "com.bruce.plugin.entity.TableInfo"));
        result.add(new VtlLightVariable("tableInfoList", file, "java.util.List<com.bruce.plugin.entity.TableInfo>"));
        result.add(new VtlLightVariable("generateService", file, "com.bruce.plugin.tool.ExtraCodeGenerateUtils"));
        return result;
    }

}
