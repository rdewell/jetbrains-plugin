package io.stacklane.jetbrains.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import io.stacklane.jetbrains.SLPlugin;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @deprecated
 *
 * @see com.intellij.openapi.module.WebModuleType
 */
public class SLModuleType extends ModuleType<SLModuleBuilder> implements ModuleTypeWithWebFeatures {
    private static final String ID = "STACKLANE_MODULE"; // likely needs to match plugin.xml -- <moduleType id="STACKLANE"

    public static SLModuleType getInstance(){
        return (SLModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    protected SLModuleType() {
        super(ID);
    }

    public static boolean isType(Module module){
        return ID.equals(ModuleType.get(module).getId());
    }

    @NotNull
    @Override
    public SLModuleBuilder createModuleBuilder() {
        return new SLModuleBuilder();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @NotNull
    @Override
    public String getName() {
        return SLPlugin.SL;
    }

    /**
     * In their example, this value matched {@link #getName()} -- unclear where used.
     */
    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDescription() {
        return getName();
    }

    @NotNull
    @Override
    public Icon getNodeIcon(boolean b) {
        return SLPlugin.getIcon();
    }

    @Override
    public boolean hasWebFeatures(@NotNull Module module) {
        return isType(module);
    }
}
