package io.stacklane.jetbrains.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * These settings will be (re)checked on the server side.
 *
 * For example, checking for ignored files on the client side helps to minimize/optimize what is uploaded.
 */
public final class UploadClientSettings {
    public static final String TEMP_DIR_PREFIX = "io.stacklane.upload.snapshots";
    public static final String ENDPOINT = "https://api.execute.website";

    public static final int MAX_FILES = 500;
    public static final int MAX_BYTES = 10000000;

    public static final String MANIFEST_FILE_NAME = "🎨.yaml";

    private static boolean isIgnoredFileName(Path path){
        try {
            return path.getFileName().toString().startsWith(".") ||
                    path.getFileName().toString().equals("__MACOSX") ||
                    Files.isHidden(path);
        } catch (Throwable ignore){}
        return false;
    }

    /**
     * esp for .git, etc.
     *
     * isHidden is OS dependent.  Windows handles hidden files / dirs differently.
     */
    public static boolean isIgnored(Path path){
        try {
            if (isIgnoredFileName(path)) return true;

            Path parent = path.getParent();

            while (parent != null){
                if (isIgnoredFileName(parent)) return true;

                parent = parent.getParent();
            }
        } catch (Throwable ignore){}
        return false;
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
