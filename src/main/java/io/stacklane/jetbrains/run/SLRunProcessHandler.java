package io.stacklane.jetbrains.run;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import io.stacklane.jetbrains.SLPlugin;
import io.stacklane.jetbrains.client.UploadClient;
import io.stacklane.jetbrains.output.BuildOutputConsoleView;
import mjson.Json;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class SLRunProcessHandler extends ProcessHandler {
    private final ConsoleView cv;
    private final Project project;
    private final Optional<Json> buildProps;

    public SLRunProcessHandler(Project project, ConsoleView cv, Optional<Json> buildProps) {
        this.cv = cv;
        this.project = project;
        this.buildProps = buildProps;
    }

    /**
     * @see com.intellij.execution.process.BaseOSProcessHandler
     */
    @Override
    public void startNotify() {
        this.addProcessListener(new MyProcessListener());

        super.startNotify();
    }

    @Override
    protected void destroyProcessImpl() {
        this.notifyProcessTerminated(0);
    }

    @Override
    protected void detachProcessImpl() {
        this.notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return null;
    }

    private class MyProcessListener implements ProcessListener {

        private void notifyTerminated() {
            SLRunProcessHandler.this.notifyProcessTerminated(0);
        }

        private boolean isStopped() {
            return isProcessTerminated() || isProcessTerminating();
        }

        /**
         * Async or the UI will lock up.
         */
        @Override
        public void startNotified(ProcessEvent processEvent) {
            CompletableFuture.runAsync(() -> {
                if (!project.getBaseDir().exists() || !project.getBaseDir().isDirectory()) {
                    cv.print("Invalid project directory", ConsoleViewContentType.ERROR_OUTPUT);
                    notifyTerminated();
                    return;
                }

                final Optional<String> manifestName = SLPlugin.readManifestName(project);

                if (!manifestName.isPresent()){
                    cv.print("A manifest file (" + SLPlugin.MANIFEST_FILE_NAME + ") " +
                                    "is required, and it must have the 'name' attribute.",
                            ConsoleViewContentType.ERROR_OUTPUT);
                    notifyTerminated();
                    return;
                }

                final BuildOutputConsoleView output = new BuildOutputConsoleView(cv, project);

                final UploadClient projectClient = UploadClient.anon(project);

                try {
                    if (!isStopped()) {
                        try {
                            projectClient.putIncrementally(output);
                        } catch (Throwable t) {
                            output.error("Error syncing files", t);
                            return;
                        }
                    }

                    if (!isStopped()) {
                        try {
                            final URI uri = projectClient.build(output, buildProps);

                            if (!isStopped()) {
                                // TODO auto-browse to URL should probably be a preference
                                BrowserUtil.browse(uri);
                            }
                        } catch (Throwable t) {
                            // assume error was already logged in console
                        }
                    }
                } finally {
                    notifyTerminated();
                }
            });
        }

        @Override
        public void processTerminated(ProcessEvent processEvent) {
            // TODO distinguish between forced stop and natural (completion) stop
            // cv.print("Stopped", ConsoleViewContentType.ERROR_OUTPUT);
        }

        @Override
        public void processWillTerminate(ProcessEvent processEvent, boolean b) {

        }

        @Override
        public void onTextAvailable(ProcessEvent processEvent, Key key) {

        }
    }

}
