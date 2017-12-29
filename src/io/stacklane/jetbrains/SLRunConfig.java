package io.stacklane.jetbrains;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Note that, in order to support automatic naming of configurations created from context,
 * your configuration should use LocatableConfigurationBase as the base class.
 *
 * This approach to generated name is from here.
 * Also this is a good example of saving run config.
 *
 * https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/JstdRunConfiguration.java
 *
 * ALSO:
 *
 * https://github.com/JetBrains/intellij-plugins/blob/master/cucumber-java/src/org/jetbrains/plugins/cucumber/java/run/CucumberJavaRunConfiguration.java
 */
public class SLRunConfig extends LocatableConfigurationBase {
    private String myGeneratedName = null;

    static final String UNNAMED = "Unnamed";

    protected SLRunConfig(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    @Nullable
    public String suggestedName() {
        String generatedName = myGeneratedName;
        if (myGeneratedName == null) {
            generatedName = generateName();
            myGeneratedName = generatedName;
        }
        return generatedName;
    }

    /*
    public String resetGeneratedName() {
        String name = generateName();
        myGeneratedName = name;
        return name;
    }*/

    @NotNull
    private String generateName(){
        return SLPluginUtil.readManifestName(this.getProject()).orElse(UNNAMED);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        // TODO ?
        return new SettingsEditor<RunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull RunConfiguration runConfiguration) {

            }

            @Override
            protected void applyEditorTo(@NotNull RunConfiguration runConfiguration) throws ConfigurationException {

            }

            @NotNull
            @Override
            protected JComponent createEditor() {
                return new JLabel("Ready");
            }
        };
    }

    /**
     * http://www.jetbrains.org/intellij/sdk/docs/basics/run_configurations/run_configuration_execution.html
     *
     * RunProfile.getState() method is called to create a RunProfileState object, describing a process about to be started. At this stage, the command line parameters, environment variables and other information required to start the process is initialized.

       RunProfileState.execute() is called. It starts the process, attaches a ProcessHandler to its input and output streams, creates a console to display the process output, and returns an ExecutionResult object aggregating the console and the process handler.

       The RunContentBuilder object is created and invoked to display the execution console in a tab of the Run or Debug tool window.
     */
    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new SLRunProfileState(executionEnvironment.getProject());
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);

    }

    public boolean isCustomConfig() {
        return UNNAMED.equals(getName()) || !getName().equals(suggestedName()) || !isGeneratedName();
    }
}
