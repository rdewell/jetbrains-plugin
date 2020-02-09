package io.stacklane.jetbrains;

import com.intellij.openapi.project.Project;
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
public final class VFSUtil {

    public static VirtualFile find(Project project, String fullPath){
        if (!fullPath.startsWith("/")) throw new IllegalArgumentException(fullPath);
        if (fullPath.endsWith("/")) throw new IllegalArgumentException(fullPath);

        for (final VirtualFile vf : ProjectRootManager.getInstance(project).getContentRoots()){
            final String[] parts = fullPath.substring(1).split("/");
            final VirtualFile c = VfsUtil.findRelativeFile(vf, parts);
            if (c != null) return c;
        }

        return null;
    }

}
