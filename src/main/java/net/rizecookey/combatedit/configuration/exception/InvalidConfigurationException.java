package net.rizecookey.combatedit.configuration.exception;

public class InvalidConfigurationException extends Exception {
    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }
}
