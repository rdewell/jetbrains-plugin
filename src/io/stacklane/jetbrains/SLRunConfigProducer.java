package io.stacklane.jetbrains;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

import java.util.Optional;

/**
 * Allows auto-configurations
 *
 * http://www.jetbrains.org/intellij/sdk/docs/basics/run_configurations/run_configuration_management.html#creating-configurations-from-context
 */
public class SLRunConfigProducer extends RunConfigurationProducer<SLRunConfig> {

    protected SLRunConfigProducer() {
        super(new SLRunConfigFactory(new SLRunConfigType()));
    }

    @Override
    protected boolean setupConfigurationFromContext(SLRunConfig slRunConfig, ConfigurationContext configurationContext, Ref<PsiElement> ref) {
        final Optional<String> readName = SLPluginUtil.readManifestName(configurationContext.getProject());

        // If no manifest name can be read, then we don't consider it automatically configurable.
        if (!readName.isPresent()) return false;

        /**
         * REF
         *
         * https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/JstdRunConfigurationProducer.java
         * https://github.com/JetBrains/intellij-plugins/blob/master/cucumber-java/src/org/jetbrains/plugins/cucumber/java/run/CucumberJavaRunConfigurationProducer.java
         */
        slRunConfig.setGeneratedName();

        return true;
    }

    /**
     * Look at both of the sources mentioned in the above method.
     * They primarily appear to be checking for configurations, not naming.
     * Therefore we do the same.
     * E.G. whether anything has been configured beyond what we consider "default".
     */
    @Override
    public boolean isConfigurationFromContext(SLRunConfig slRunConfig, ConfigurationContext configurationContext) {
        if (slRunConfig.getBuildProps() != null && !slRunConfig.getBuildProps().isEmpty()){
            return false;
        }

        return true;
    }
}
