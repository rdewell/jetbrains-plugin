package io.stacklane.jetbrains.project;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import io.stacklane.jetbrains.SLPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * For IntellIJ only (not WebStorm), to basically create a group of {@link ProjectTemplate}'s.
 */
public class SLProjectTemplateFactory extends ProjectTemplatesFactory {

    @NotNull
    @Override
    public String[] getGroups() {
        return new String[]{SLPlugin.SL /*, WebModuleBuilder.GROUP_NAME*/};
    }

    @Override
    public Icon getGroupIcon(String group) {
        return SLPlugin.SL.equals(group) ? SLPlugin.getIcon() : null;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String s, WizardContext wizardContext) {
        return new ProjectTemplate[]{new SLHelloWorldTemplate("Hello World")};
    }

}
