package com.bruce.plugin.scratch;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.psi.JsonLiteral;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author bruce ge 2023/3/31
 */
public class GroupJsonCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        PsiElement parent1 = position.getParent();
        if(parent1==null){
            return;
        }
        PsiFile originalFile = parameters.getOriginalFile();
        VirtualFile virtualFile = originalFile.getVirtualFile();
        if(virtualFile!=null){

            String path = virtualFile.getPath();
            String currentBaseDir = MyScratchUtils.getCurrentBaseDir(originalFile.getProject(), virtualFile);
            if(currentBaseDir!=null&&path.equals(MyScratchUtils.getEasyCodeGroupFile(currentBaseDir))){
                if (parent1 instanceof JsonValue) {
                    if (parent1 instanceof JsonLiteral) {
                        PsiElement parent = parent1.getParent();
                        if (parent instanceof JsonProperty) {
                            JsonProperty jsonProperty = (JsonProperty) parent;
                            String name = jsonProperty.getName();
                            JsonLiteral literal = (JsonLiteral) parent1;
                            if(name.equals(MyScratchUtils.TEMPLATE_NAME)){
                                String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeTemplateDirectory(currentBaseDir);
                                File file = new File(easyCodeTemplateDirectory);
                                File[] files = file.listFiles();
                                for (File file1 : files) {
                                    result.addElement(LookupElementBuilder.create(file1.getName()));
                                }
                            } else if(name.equals(MyScratchUtils.GLOBALCONFIGNAME)){
                                String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeSubDirectory(currentBaseDir,MyScratchUtils.GLOBAL_CONFIG);
                                File file = new File(easyCodeTemplateDirectory);
                                File[] files = file.listFiles();
                                for (File file1 : files) {
                                    result.addElement(LookupElementBuilder.create(file1.getName()));
                                }
                            } else if (name.equals(MyScratchUtils.COLUMNCONFIGNAME)) {
                                String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeSubDirectory(currentBaseDir,MyScratchUtils.COLUMN_CONFIG);
                                File file = new File(easyCodeTemplateDirectory);
                                File[] files = file.listFiles();
                                for (File file1 : files) {
                                    result.addElement(LookupElementBuilder.create(file1.getName()));
                                }
                            } else if (name.equals(MyScratchUtils.TYPEMAPPERCONFIGNAME)) {
                                String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeSubDirectory(currentBaseDir,MyScratchUtils.TYPE_MAPPER_CONFIG);
                                File file = new File(easyCodeTemplateDirectory);
                                File[] files = file.listFiles();
                                for (File file1 : files) {
                                    result.addElement(LookupElementBuilder.create(file1.getName()));
                                }

                            }
                        }
                    }
                }
            }
        }
        super.fillCompletionVariants(parameters, result);
    }
}
