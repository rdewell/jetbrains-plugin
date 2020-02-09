package io.stacklane.jetbrains.output;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;

import java.nio.file.Path;

/**
 * Adapts {@link ConsoleView}
 */
public class BuildOutputConsoleView implements BuildOutputConsole {
    private final ConsoleView cv;
    private final Path source;

    public BuildOutputConsoleView(ConsoleView cv, Path source) {
        this.cv = cv;
        this.source = source;
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
                    // TODO if we can find this in source, let's make it a hyper link -- but need to know correct way to open a file from plugin
                    cv.print(entry.getFilePath(), contentType);
                    cv.print(" ", contentType);
                }

                if (entry.getMessage() != null) cv.print(entry.getMessage(), contentType);

                // TODO if there is source code, then display in a popup after clicking hyperlink?

                cv.print("\n", contentType);

                break;
            }
        }
    }

}
