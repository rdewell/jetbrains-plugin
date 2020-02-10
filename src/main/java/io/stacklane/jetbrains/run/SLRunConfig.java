package io.stacklane.jetbrains.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import io.stacklane.jetbrains.SLPlugin;
import mjson.Json;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * TODO this contains secure info not to be shared -- how we can disable the 'share' checkbox?
 *
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

    public String getBuildProps() {
        return buildProps;
    }

    public void setBuildProps(String buildProps) {
        this.buildProps = buildProps;
    }

    private String buildProps = null;

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

    @NotNull
    private String generateName(){
        return SLPlugin.readManifestName(getProject()).orElse(UNNAMED);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new SLRunConfigEditor(getProject());
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
        Optional<Json> json = Optional.empty();

        if (buildProps != null && !buildProps.isEmpty()){
            try {
                Json read = Json.read(buildProps);
                json = Optional.of(read);
            } catch (Throwable t){
                throw new ExecutionException("Invalid JSON build properties.");
            }
        }

        return new SLRunProfileState(executionEnvironment.getProject(), json);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        this.buildProps = readString(element, "buildProps", "");
        if (this.buildProps.isEmpty()) buildProps = null;
    }

    @Override
    public void checkSettingsBeforeRun() throws RuntimeConfigurationException {
        if (buildProps != null && !buildProps.isEmpty()){
            try {
                Json.read(buildProps);
            } catch (Throwable t){
                throw new RuntimeConfigurationException("Invalid JSON build properties.");
            }
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeString(element, "buildProps", buildProps == null ? "" : buildProps);
    }

    private static String readString(@NotNull Element element, @NotNull String key, @NotNull String defaultValue) {
        // Says use XmlSerializer now
        String value = JDOMExternalizer.readString(element, key);
        return value != null ? value : defaultValue;
    }

    private static void writeString(@NotNull Element element, @NotNull String key, @NotNull String value) {
        // Says use XmlSerializer now
        JDOMExternalizer.write(element, key, value);
    }

}
