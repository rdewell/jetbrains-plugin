package io.stacklane.jetbrains.output;

import mjson.Json;

import java.net.URI;

/**
 *
 */
public class BuildOutputEntry {
    private final BuildOutputEntryType type;
    private String message;
    private URI result;
    private String filePath;

    public static BuildOutputEntry info(String message){
        return new BuildOutputEntry(BuildOutputEntryType.INFO, message);
    }

    public static BuildOutputEntry error(String message) {
        return new BuildOutputEntry(BuildOutputEntryType.ERROR, message);
    }

    public static BuildOutputEntry parse(String jsonString){
        try {
            final Json json = Json.read(jsonString);
            return new BuildOutputEntry(json);
        } catch (Throwable t){
            return error(t.getMessage());
        }
    }

    private BuildOutputEntry(BuildOutputEntryType type, String message){
        this.type = type;
        this.message = message;
    }

    private BuildOutputEntry(final Json json){
        type = BuildOutputEntryType.parse(json.at("type").asString());

        if (json.has("message")) message = json.at("message").asString();
        if (json.has("file")) filePath = json.at("file").asString();

        if (type == BuildOutputEntryType.RESULT){
            result = URI.create(json.at("url").asString());
        }
    }

    public String getFilePath(){
        return filePath;
    }

    public BuildOutputEntryType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public URI getResult(){
        return result;
    }

}
