package io.stacklane.jetbrains.client;

/**
 * Decouple console.
 */
public interface UploadConsole {

    void error(String msg);
    void error(String msg, Throwable t);
    void info(String msg);

}
