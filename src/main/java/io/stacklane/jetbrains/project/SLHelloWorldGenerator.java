package io.stacklane.jetbrains.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import io.stacklane.jetbrains.SLPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 */
public class SLHelloWorldGenerator extends DirectoryProjectGeneratorBase {

    @NotNull
    @Override
    public String getName() {
        return SLPlugin.SL;
    }

    @Nullable
    @Override
    public Icon getLogo() {
        return SLPlugin.getIcon();
    }

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile virtualFile,
                                @NotNull Object o, @NotNull Module module) {
        new SLHelloWorldTemplate(SLPlugin.SL).generateProject(project, virtualFile, o, module);
    }

}
