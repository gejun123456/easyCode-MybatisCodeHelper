package com.bruce.plugin.scratch;

import com.intellij.ide.extensionResources.ExtensionsRootType;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author bruce ge 2023/3/28
 */
public class EasyCodeEditorNotificationProvider extends EditorNotifications.Provider<EasyCodeNotificationPanel> implements DumbAware {
    private static final Key<EasyCodeNotificationPanel> KEY = Key.create("easycode.mybatiscodehelper.notificationPanel");
    @Override
    public @NotNull Key<EasyCodeNotificationPanel> getKey() {
        return KEY;
    }

    @Override
    public @Nullable EasyCodeNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor, @NotNull Project project) {
        //check if virtual file in under easyCode scratch folder.
        String path = file.getPath();
        String easyCodePath = MyScratchUtils.getEasyCodeDirectory();
        if(path.startsWith(easyCodePath)){
            return new EasyCodeNotificationPanel(file,fileEditor,project);
        }
        return null;
    }


}
