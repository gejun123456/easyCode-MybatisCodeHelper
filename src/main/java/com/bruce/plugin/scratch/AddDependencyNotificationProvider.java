package com.bruce.plugin.scratch;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author bruce ge 2023/4/6
 */
public class AddDependencyNotificationProvider extends EditorNotifications.Provider<AddDependencyPanel> {
    private static final Key<AddDependencyPanel> KEY = Key.create("easycode.mybatiscodehelper.addDependencyPanel");
    @Override
    public @NotNull Key<AddDependencyPanel> getKey() {
        return KEY;
    }

    @Override
    public @Nullable AddDependencyPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor, @NotNull Project project) {
        //check if virtual file in under easyCode scratch folder.
        String path = file.getPath();
//        String easyCodePath = MyScratchUtils.getEasyCodeDirectory();
//        if(path.startsWith(easyCodePath)){
//            return new AddDependencyPanel(file,fileEditor,project);
//        }
        return null;
    }
}
