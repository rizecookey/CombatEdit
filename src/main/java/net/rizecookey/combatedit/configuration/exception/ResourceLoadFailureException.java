package net.rizecookey.combatedit.configuration.exception;

import java.io.IOException;

public class ResourceLoadFailureException extends RuntimeException {
    public ResourceLoadFailureException(IOException cause) {
        super("Could not load resource", cause);
    }
}
