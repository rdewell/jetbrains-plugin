package io.stacklane.jetbrains.module;

import com.intellij.ide.util.projectWizard.EmptyWebProjectTemplate;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import io.stacklane.jetbrains.SLPlugin;
import io.stacklane.jetbrains.util.VfsLookup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @deprecated
 *
 * https://www.jetbrains.org/intellij/sdk/docs/tutorials/project_wizard/adding_new_steps.html
 * https://www.jetbrains.org/intellij/sdk/docs/reference_guide/project_model/module.html
 *
 * @see com.intellij.openapi.module.WebModuleBuilder
 */
public class SLModuleBuilder extends ModuleBuilder  {

    private final EmptyWebProjectTemplate template;
    private final NotNullLazyValue<ProjectGeneratorPeer<Object>> lazyPeer;

    public SLModuleBuilder() {
        this.template = new EmptyWebProjectTemplate();
        this.lazyPeer = template.createLazyPeer();
    }

    @Override
    public ModuleType<SLModuleBuilder> getModuleType() {
        return SLModuleType.getInstance();
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
        /**
         * @see com.intellij.openapi.module.WebModuleBuilder#setupRootModel
         */
        doAddContentEntry(rootModel);
    }

    /**
     * @see com.intellij.openapi.module.WebModuleBuilder#commitModule
     */
    @Nullable
    @Override
    public Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
        Objects.requireNonNull(project, "project");

        final Module module = super.commitModule(project, model);

        if (module != null && this.template != null) {
            this.doGenerate(this.template, module);
        }

        return module;
    }

    private void doGenerate(@NotNull WebProjectTemplate<Object> template, @NotNull Module module) {
        SLPlugin.initManifest(module);

        template.generateProject(module.getProject(), VfsLookup.getModuleRoot(module), (this.lazyPeer.getValue()).getSettings(), module);
    }

}
