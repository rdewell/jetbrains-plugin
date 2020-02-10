package io.stacklane.jetbrains.project;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.stacklane.jetbrains.SLPlugin;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 */
public class SLHelloWorldTemplate extends WebProjectTemplate<Object> {

    private final String name;

    public SLHelloWorldTemplate(String name){
        this.name = name;
    }

    @Override
    public String getDescription() {
        return "Get started with a Stacklane template.";
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return SLPlugin.getIcon();
    }

    public Icon getLogo() {
        return SLPlugin.getIcon();
    }

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile virtualFile,
                                @NotNull Object o, @NotNull Module module) {
        final Map<String,String> files = new HashMap<>();
        files.put(SLPlugin.MANIFEST_FILE_NAME, "name: " + module.getName());
        files.put(SLPlugin.STYLE_SETTINGS, "$title: 'Hello World';");
        try {
            files.put("index.html", resourceToString("HelloWorld.html"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        SLPlugin.createChildFiles(module, files);
    }

    public static String resourceToString(String filePath) throws IOException {
        try (InputStream inputStream = SLHelloWorldTemplate.class.getResourceAsStream(filePath)) {
            return inputStreamToString(inputStream);
        }
    }

    private static String inputStreamToString(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
