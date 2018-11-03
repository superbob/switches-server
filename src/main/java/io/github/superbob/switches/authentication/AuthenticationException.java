package io.github.superbob.switches.authentication;

public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = -4219581831334158651L;

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
