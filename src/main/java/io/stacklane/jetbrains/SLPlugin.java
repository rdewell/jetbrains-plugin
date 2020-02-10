package io.stacklane.jetbrains;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import io.stacklane.jetbrains.util.VfsLookup;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Map;

/**
 *
 */
public final class SLPlugin {
    public static final String SL = "Stacklane";
    public static final String SL_RUNNER = "Stacklane Runner";

    public static final int MAX_FILES = 500;
    public static final int MAX_BYTES = 5000000;
    public static final String MANIFEST_FILE_NAME = "🎛.yaml";
    public static final String STYLE_SETTINGS = "🎨.scss";

    /**
     * See:
     *
     * https://www.jetbrains.org/intellij/sdk/docs/reference_guide/work_with_icons_and_images.html
     */
    public static Icon getIcon(){
        return IconLoader.getIcon("icon16.png", SLPlugin.class);
    }

    public static void createChildFiles(final Module module, final Map<String,String> files){
        /**
         * File changes must be from {@link com.intellij.openapi.application.Application#runWriteAction(Runnable)}
         */
        ApplicationManager.getApplication().runWriteAction(() -> {
            final VirtualFile dir = VfsLookup.getModuleRoot(module);

            try {
                for (final String name : files.keySet()){
                    if (dir.findChild(name) != null) continue;
                    final String data = files.get(name);
                    final VirtualFile manifest = dir.createChildData("" /**?**/, name);
                    manifest.setBinaryContent(data.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public static Optional<String> readManifestName(Project project){
        for (final Module module : ModuleManager.getInstance(project).getModules()){
            final Optional<String> f = readManifestName(module);
            if (f.isPresent()) return f;
        }
        return Optional.empty();
    }

    /**
     * @return {@link Optional#empty()} in the case that either the manifest doesn't exist, or it doesn't define a name.
     */
    public static Optional<String> readManifestName(Module module){
        final VirtualFile moduleRoot = VfsLookup.getModuleRoot(module);
        final VirtualFile manifest = moduleRoot.findChild(MANIFEST_FILE_NAME);

        if (manifest == null) return Optional.empty();

        try (final InputStream is = manifest.getInputStream()) {
            final String name = readManifestName(is);

            if (name == null) return Optional.empty();

            return Optional.of(name);
        } catch (Throwable throwable) {
            return Optional.empty();
        }
    }

    /**
     * Use a simple read line technique instead of a full blown YAML parser.
     *
     * WARNING: This does not close the {@link InputStream}
     */
    public static String readManifestName(InputStream is){
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String currentLine = null;

            while ((currentLine = in.readLine()) != null) {
                if (currentLine.startsWith("name: ")){
                    String possible = currentLine.split("\\s+", 2)[1];
                    if (possible.trim().length() > 0){
                        return possible;
                    } else {
                        return null;
                    }
                }
            }
            return null;
        } catch (Throwable t){
            return null;
        }
    }
}
