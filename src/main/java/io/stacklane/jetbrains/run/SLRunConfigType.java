package io.stacklane.jetbrains.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.project.DumbAware;
import io.stacklane.jetbrains.SLPlugin;

/**
 * REF
 *
 * https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/JstdConfigurationType.java
 */
public class SLRunConfigType extends ConfigurationTypeBase implements DumbAware {

    public SLRunConfigType() {
        super(SLPlugin.SL, SLPlugin.SL, SLPlugin.SL_RUNNER, SLPlugin.getIcon());

        addFactory(new SLRunConfigFactory(this));
    }

}
