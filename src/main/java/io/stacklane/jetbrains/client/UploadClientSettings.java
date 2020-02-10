package io.stacklane.jetbrains.client;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * These settings will be (re)checked on the server side.
 *
 * For example, checking for ignored files on the client side helps to minimize/optimize what is uploaded.
 */
final class UploadClientSettings {
    public static final String TEMP_DIR_PREFIX = "io.stacklane.upload.snapshots";
    public static final String ENDPOINT = "https://api.execute.website";

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
}
