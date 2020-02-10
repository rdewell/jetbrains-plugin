package io.stacklane.jetbrains.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import mjson.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 *
 */
public class SLRunProfileState implements RunProfileState {
    private final Project project;
    private final Optional<Json>  buildProps;

    public SLRunProfileState(Project project, Optional<Json> buildProps) {
        this.project = project;
        this.buildProps = buildProps;
    }

    /**
     * @see CommandLineState#execute
     */
    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException {
        final ConsoleView cv = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

        final SLRunProcessHandler processStarted = new SLRunProcessHandler(project, cv, buildProps);

        return new ExecutionResult() {
            @Override
            public ExecutionConsole getExecutionConsole() {
                return cv;
            }

            @NotNull
            @Override
            public AnAction[] getActions() {
                return new AnAction[0]; // must not return null
            }

            @Override
            public ProcessHandler getProcessHandler() {
                return processStarted;
            }
        };
    }
}
