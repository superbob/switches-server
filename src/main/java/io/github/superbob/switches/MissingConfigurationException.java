package io.github.superbob.switches;

public class MissingConfigurationException extends RuntimeException {
    private static final long serialVersionUID = -4903908316576862288L;

    public MissingConfigurationException() {
    }

    public MissingConfigurationException(String message) {
        super(message);
    }

    public MissingConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingConfigurationException(Throwable cause) {
        super(cause);
    }
}
