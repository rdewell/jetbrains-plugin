package io.stacklane.jetbrains;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
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
        final SLRunConfig s = new SLRunConfig(project, this, "Stacklane Runner" /* unclear where this gets used */);
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
    public boolean canConfigurationBeSingleton() {
        // this was false while other singleton setting above was true, in
        // https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/JstdConfigurationType.java
        return false;
    }

}
