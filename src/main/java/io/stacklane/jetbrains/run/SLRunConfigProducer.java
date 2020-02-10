package io.stacklane.jetbrains.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import io.stacklane.jetbrains.SLPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Allows auto-configurations
 *
 * http://www.jetbrains.org/intellij/sdk/docs/basics/run_configurations/run_configuration_management.html#creating-configurations-from-context
 */
public class SLRunConfigProducer extends LazyRunConfigurationProducer<SLRunConfig> {

    protected SLRunConfigProducer() {
        super();
    }

    private static boolean isRunAvailable(Module module){
        // Preferred
        //if (SLModuleType.isType(module)) return true;

        // Fallback
        final Optional<String> readName = SLPlugin.readManifestName(module);
        if (readName.isPresent()) return true;

        return false;
    }

    @Override
    protected boolean setupConfigurationFromContext(SLRunConfig slRunConfig, ConfigurationContext configurationContext, Ref<PsiElement> ref) {
        if (!isRunAvailable(configurationContext.getModule())) return false;

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
        if (slRunConfig.getBuildProps() != null &&
            !slRunConfig.getBuildProps().isEmpty()){
            return false;
        }

        return true;
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return new SLRunConfigFactory(new SLRunConfigType());
    }
}
