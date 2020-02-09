package io.stacklane.jetbrains.output;

/**
 *
 */
public interface BuildOutputConsole {

    default void info(String msg){
        entry(BuildOutputEntry.info(msg));
    }

    default void error(String msg){
        entry(BuildOutputEntry.error(msg));
    }

    default void error(String msg, Throwable t){
        entry(BuildOutputEntry.error(msg + " - " + t.getMessage()));
    }

    void entry(BuildOutputEntry entry);

}
