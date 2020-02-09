package io.stacklane.jetbrains;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * You can customize additional aspects of your configuration factory by overriding the getIcon,
 * getAddIcon, getName and the default settings methods. These additional overrides are optional.
 */
class SLRunConfigFactory extends ConfigurationFactory {
    protected SLRunConfigFactory(SLRunConfigType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        final SLRunConfig s = new SLRunConfig(project, this, SLPluginUtil.SL_RUNNER);
        return s;
    }

    /**
     * Corresponds to initial state of the checkbox "Single instance only".
     */
    @Override
    public boolean isConfigurationSingletonByDefault() {
        return true;
    }

    /**
     * Corresponds to the checkbox "Single instance only".
     */
    @Override
    public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
    }

}
