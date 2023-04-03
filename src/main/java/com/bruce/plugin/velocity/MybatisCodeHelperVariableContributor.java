package com.bruce.plugin.velocity;

import com.bruce.plugin.entity.GlobalConfig;
import com.bruce.plugin.entity.GlobalConfigGroup;
import com.bruce.plugin.tool.CurrGroupUtils;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.velocity.VtlGlobalVariableProvider;
import com.intellij.velocity.psi.VtlImplicitVariable;
import com.intellij.velocity.psi.VtlInterpolation;
import com.intellij.velocity.psi.VtlLightVariable;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.directives.VtlSet;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileType;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author bruce ge 2023/3/6
 */
public class MybatisCodeHelperVariableContributor extends VtlGlobalVariableProvider {
    @Override
    public @NotNull Collection<VtlVariable> getGlobalVariables(@NotNull VtlFile vtlFile) {
        //should use vtl to provide code completion for users.
        Collection<VtlInterpolation> childrenOfAnyType = PsiTreeUtil.findChildrenOfType(vtlFile, VtlInterpolation.class);
        List<VtlVariable> result=  Lists.newArrayList();
        for (VtlInterpolation vtlInterpolation : childrenOfAnyType) {
            String text = vtlInterpolation.getText();
            if(text.startsWith("$!{")){
                if(text.contains("mybatisCodehelper.vm")){
                    //find by file name.
                    GlobalConfigGroup currGlobalConfigGroup = CurrGroupUtils.getCurrGlobalConfigGroup();
                    List<GlobalConfig> elementList = currGlobalConfigGroup.getElementList();
                    for (GlobalConfig globalConfig : elementList) {
                        String name = globalConfig.getName();
                        if(name.equals("mybatisCodehelper.vm")){
                            String value = globalConfig.getValue();
                            //create vtl file by value. create file from text.
                            PsiFile fileFromText = PsiFileFactory.getInstance(vtlFile.getProject()).createFileFromText("mybatisCodehelper.vm", VtlFileType.INSTANCE, value);
                            PsiFile psiFile = fileFromText;
                            Collection<VtlSet> childrenOfType = PsiTreeUtil.findChildrenOfType(psiFile, VtlSet.class);
                            for (VtlSet vtlSet : childrenOfType) {
                                Collection<VtlReferenceExpression> childrenOfType1 = PsiTreeUtil.findChildrenOfType(vtlSet, VtlReferenceExpression.class);
                                for (VtlReferenceExpression vtlReferenceExpression : childrenOfType1) {
                                    String text1 = vtlReferenceExpression.getText();
                                    PsiType psiType = vtlReferenceExpression.getPsiType();
                                    result.add(new VtlLightVariable(text1,vtlFile, psiType.getCanonicalText()));
                                }
                            }

                        }
                    }
                }
            }
        }
        return result;
    }
}
