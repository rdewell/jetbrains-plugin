package io.stacklane.jetbrains;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import io.stacklane.jetbrains.client.UploadConsole;

/**
 *
 */
class ConsoleWrapper implements UploadConsole {
    private final ConsoleView cv;

    public ConsoleWrapper(ConsoleView cv) {
        this.cv = cv;
    }

    public void error(String msg) {
        cv.print(msg + "\n", ConsoleViewContentType.ERROR_OUTPUT);
    }

    @Override
    public void error(String msg, Throwable t) {
        cv.print(msg  + " -- " + t.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
    }

    public void info(final String msg) {
        if (msg.startsWith("http://") || msg.startsWith("https://")){
            cv.printHyperlink(msg + "\n", project -> BrowserUtil.browse(msg));
        } else {
            cv.print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }
}
