package io.stacklane.jetbrains.output;

/**
 *
 */
public enum BuildOutputEntryType {

    GROUP_BEGIN,

    GROUP_END,

    DEBUG,

    ERROR,

    INFO,

    WARN,

    RESULT;

    static BuildOutputEntryType parse(String id){
        switch (id){
            case "group-begin": return GROUP_BEGIN;
            case "group-end": return GROUP_END;
            case "debug": return DEBUG;
            case "info": return INFO;
            case "warn": return WARN;
            case "result": return RESULT;
            default: return ERROR;
        }
    }

}
