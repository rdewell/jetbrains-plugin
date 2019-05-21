package io.stacklane.jetbrains;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.project.DumbAware;

import javax.swing.*;


/**
 * REF
 *
 * https://github.com/JetBrains/intellij-plugins/blob/master/JsTestDriver/src/com/google/jstestdriver/idea/execution/JstdConfigurationType.java
 */
public class SLRunConfigType extends ConfigurationTypeBase implements DumbAware {

    private static final Icon i = new ImageIcon(SLRunConfigType.class.getResource("sl-16.png"));

    public SLRunConfigType() {
        super("Stacklane", "Stacklane", "Stacklane Runner", i);

        addFactory(new SLRunConfigFactory(this));
    }

}
