package io.stacklane.jetbrains.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/settings/ui/JstdRunConfigurationEditor.java
 */
public class SLRunConfigEditor extends SettingsEditor<SLRunConfig> {

    private final Project project;
    private final JComponent myRootComponent;
    private final JTextArea buildProps;

    public SLRunConfigEditor(Project project){
        this.project = project;

        buildProps = new JTextArea("", 20, 30);

        JBTabbedPane tabbedPane = new JBTabbedPane();

        tabbedPane.addTab("Build Properties (JSON)", buildProps);

        tabbedPane.setSelectedIndex(0);

        myRootComponent = tabbedPane;
    }

    @Override
    protected void resetEditorFrom(@NotNull SLRunConfig slRunConfig) {
        buildProps.setText(slRunConfig.getBuildProps());
    }

    @Override
    protected void applyEditorTo(@NotNull SLRunConfig slRunConfig) throws ConfigurationException {
        slRunConfig.setBuildProps(buildProps.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myRootComponent;
    }
}



