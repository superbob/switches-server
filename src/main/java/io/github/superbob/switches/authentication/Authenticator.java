package io.github.superbob.switches.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

public class Authenticator {
    private static final String BEARER_PREFIX = "Bearer ";

    private final GoogleIdTokenVerifier verifier;

    public Authenticator(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    public String authenticate(final String authentication) {
        if (authentication == null) {
            throw new AuthenticationException("No authentication, provide an authentication bearer token");
        }
        if (!authentication.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException("Wrong authentication type, use Bearer");
        }
        final String idTokenString = authentication.substring(BEARER_PREFIX.length());
        final GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new AuthenticationException("Invalid ID token");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new AuthenticationException("Error while verifying ID token", e);
        }

        final GoogleIdToken.Payload payload = idToken.getPayload();
        if (payload == null) {
            throw new AuthenticationException("Empty ID token");
        }
        return payload.getEmail();
    }
}
