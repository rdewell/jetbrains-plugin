package io.stacklane.jetbrains.client;

import org.apache.http.HttpResponse;

/**
 *
 */
public class UploadStatusException extends Exception {
    private final int status;

    public UploadStatusException(int status){
        super("Status: " + status);
        this.status = status;
    }

    public UploadStatusException(HttpResponse response) {
        this(response.getStatusLine().getStatusCode());
    }

    public int getStatus(){
        return status;
    }
}
