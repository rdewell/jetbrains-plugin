package io.stacklane.jetbrains.output;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.HyperlinkWithPopupMenuInfo;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.stacklane.jetbrains.VFSUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Adapts {@link ConsoleView}
 */
public class BuildOutputConsoleView implements BuildOutputConsole {
    private final ConsoleView cv;
    private final Project project;

    public BuildOutputConsoleView(ConsoleView cv, Project project) {
        this.cv = cv;
        this.project = project;
    }

    @Override
    public void entry(BuildOutputEntry entry) {
        switch (entry.getType()){
            case RESULT:{
                final String u = entry.getResult().toString();
                cv.printHyperlink(u + "\n", project -> BrowserUtil.browse(u));
                break;
            }
            case GROUP_BEGIN:{
                cv.print(entry.getMessage() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                break;
            }
            case GROUP_END:{

                break;
            }
            default:{
                final ConsoleViewContentType contentType;

                switch (entry.getType()){
                    case ERROR:{
                        contentType = ConsoleViewContentType.LOG_ERROR_OUTPUT;
                        break;
                    }
                    case WARN:{
                        contentType = ConsoleViewContentType.LOG_WARNING_OUTPUT;
                        break;
                    }
                    case DEBUG:{
                        contentType = ConsoleViewContentType.LOG_DEBUG_OUTPUT;
                        break;
                    }
                    default:{
                        //contentType = ConsoleViewContentType.LOG_INFO_OUTPUT; // green
                        contentType = ConsoleViewContentType.NORMAL_OUTPUT; // black
                        break;
                    }
                }

                /*
                 * {
                 *   "type":"error",
                 *   "message":"Type Mismatch: expected (string=\"x\"), was number",
                 *   "group":{"name":"Endpoints","id":"grp-12"}
                 *   "file":"/GET.js",
                 *   "line":{"end":8,"begin":8},
                 *   "source":{"offset":5,"type":"js","value":"}\nlet v = 'x';\nv = 5;\n({});\n"}
                 * }
                 */

                if (entry.getFilePath() != null){
                    final VirtualFile found = VFSUtil.find(project, entry.getFilePath());

                    if (found != null){
                        /**
                         * TODO note that {@link OpenFileDescriptor} can accept offset  / line number
                         *
                         * TODO maybe displaying the full path is too much
                         */
                        cv.printHyperlink(entry.getFilePath(), project -> new OpenFileDescriptor(project, found).navigate(true));
                    } else {
                        cv.print(entry.getFilePath(), contentType);
                    }

                    cv.print(" ", contentType);
                }

                if (entry.getMessage() != null) cv.print(entry.getMessage(), contentType);

                /**
                 * TODO see {@link HyperlinkWithPopupMenuInfo}
                 */

                cv.print("\n", contentType);

                break;
            }
        }
    }

}
