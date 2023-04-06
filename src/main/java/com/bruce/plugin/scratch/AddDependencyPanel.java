package com.bruce.plugin.scratch;

import com.bruce.plugin.dict.GlobalDict;
import com.bruce.plugin.dto.ColumnTableData;
import com.bruce.plugin.dto.GroupInfo;
import com.bruce.plugin.entity.TableInfo;
import com.bruce.plugin.entity.Template;
import com.bruce.plugin.entity.TypeMapper;
import com.bruce.plugin.enums.MatchType;
import com.bruce.plugin.service.CodeGenerateService;
import com.bruce.plugin.service.TableInfoSettingsService;
import com.bruce.plugin.tool.CacheDataUtils;
import com.bruce.plugin.tool.CollectionUtil;
import com.bruce.plugin.tool.ProjectUtils;
import com.bruce.plugin.tool.VelocityResult;
import com.bruce.plugin.ui.JTableDialog;
import com.bruce.plugin.ui.base.EditorSettingsInit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasNamespace;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.JBIterable;
import kotlin.text.Charsets;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author bruce ge 2023/4/6
 */
public class AddDependencyPanel extends EditorNotificationPanel {
    public AddDependencyPanel(VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
        super();
        setText("easy code files");

        String path = virtualFile.getPath();
//        String easyCodeTemplateDirectory = MyScratchUtils.getEasyCodeTemplateDirectory();
//        if (path.startsWith(easyCodeTemplateDirectory)) {
//            createActionLabel("add dependency", new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println("hello world");
//                }
//            });
//        }
    }





}
