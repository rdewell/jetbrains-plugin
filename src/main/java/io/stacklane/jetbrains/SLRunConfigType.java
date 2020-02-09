package io.stacklane.jetbrains;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.IconLoader;

/**
 * REF
 *
 * https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/JstdConfigurationType.java
 */
public class SLRunConfigType extends ConfigurationTypeBase implements DumbAware {

    public SLRunConfigType() {
        super(SLPluginUtil.SL, SLPluginUtil.SL, SLPluginUtil.SL_RUNNER, SLPluginUtil.getIcon());

        addFactory(new SLRunConfigFactory(this));
    }

}
