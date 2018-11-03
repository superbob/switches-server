package io.github.superbob.switches.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

class AuthenticatorTest {
    private Authenticator authenticator;
    private GoogleIdTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = mock(GoogleIdTokenVerifier.class);
        authenticator = new Authenticator(verifier);
    }

    @Test
    @DisplayName("should throw exception when given null authentication")
    void nullAuthenticationThrows() {
        assertThrows(AuthenticationException.class, () -> authenticator.authenticate(null));

        verifyZeroInteractions(verifier);
    }

    @Test
    @DisplayName("should throw exception when given malformed authentication")
    void malformedAuthenticationThrows() {
        assertThrows(AuthenticationException.class, () -> authenticator.authenticate("Malformed"));

        verifyZeroInteractions(verifier);
    }

    @Test
    @DisplayName("should throw exception when given bad bearer authentication")
    void badBearerAuthenticationThrows() throws GeneralSecurityException, IOException {
        when(verifier.verify(anyString())).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> authenticator.authenticate("Bearer badtoken"));

        verify(verifier).verify("badtoken");
    }

    @Test
    @DisplayName("should throw exception when given exception in verifier")
    void exceptionInVerifierRethrows() throws GeneralSecurityException, IOException {
        when(verifier.verify(anyString())).thenThrow(new GeneralSecurityException());

        assertThrows(AuthenticationException.class, () -> authenticator.authenticate("Bearer anything"));

        verify(verifier).verify("anything");
    }

    @Test
    @DisplayName("should return principal when given good bearer authentication")
    void goodAuthenticationReturnEmail() throws GeneralSecurityException, IOException {
        final GoogleIdToken googleIdToken = mock(GoogleIdToken.class);
        final GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(verifier.verify(anyString())).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("principal@domain.com");

        final String principal = authenticator.authenticate("Bearer good.tok.en");

        assertThat(principal).isEqualTo("principal@domain.com");

        verify(verifier).verify("good.tok.en");
    }

    @Test
    @DisplayName("should throw exception when given null payload")
    void exceptionNullPayload() throws GeneralSecurityException, IOException {
        final GoogleIdToken googleIdToken = mock(GoogleIdToken.class);
        when(verifier.verify(anyString())).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> authenticator.authenticate("Bearer null"));

        verify(verifier).verify("null");
    }

}
