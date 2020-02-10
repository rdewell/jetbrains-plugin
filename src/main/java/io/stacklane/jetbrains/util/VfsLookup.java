package io.stacklane.jetbrains.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;

/**
 * @see VfsUtil
 * @see VfsUtilCore
 * @see FilenameIndex
 */
public final class VfsLookup {

    public static VirtualFile getModuleRoot(Module module){
        /**
         * Logic from {@link com.intellij.openapi.module.WebModuleBuilder}
         */
        final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        final VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
        final VirtualFile dir;;
        if (contentRoots.length > 0 && contentRoots[0] != null) {
            dir = contentRoots[0];
        } else {
            dir = module.getProject().getBaseDir();
        }
        return dir;
    }

    public static VirtualFile find(Module module, String fullPath){
        if (!fullPath.startsWith("/")) throw new IllegalArgumentException(fullPath);
        if (fullPath.endsWith("/")) throw new IllegalArgumentException(fullPath);

        final String[] parts = fullPath.substring(1).split("/");
        final VirtualFile c = VfsUtil.findRelativeFile(getModuleRoot(module), parts);
        if (c != null) return c;

        return null;
    }

    /**
     * @deprecated  Always use {@link #find(Module, String)} ?
     */
    public static VirtualFile find(Project project, String fullPath){
        if (!fullPath.startsWith("/")) throw new IllegalArgumentException(fullPath);
        if (fullPath.endsWith("/")) throw new IllegalArgumentException(fullPath);

        final String[] parts = fullPath.substring(1).split("/");

        for (final VirtualFile vf : ProjectRootManager.getInstance(project).getContentRoots()){
            final VirtualFile c = VfsUtil.findRelativeFile(vf, parts);
            if (c != null) return c;
        }

        return null;
    }

}
