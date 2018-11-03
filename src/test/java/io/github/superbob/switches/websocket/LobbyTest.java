package io.github.superbob.switches.websocket;

import io.github.superbob.switches.authentication.Authenticator;
import io.github.superbob.switches.websocket.mutable.MutableWebSocketActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ratpack.websocket.WebSocket;
import ratpack.websocket.internal.DefaultWebSocketMessage;

import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

class LobbyTest {
    private static final String PRINCIPAL = "principal@domain.com";
    private Authenticator authenticator;
    private Lobby lobby;
    private BiConsumer<WebSocket, MutableWebSocketActions> forward;

    @BeforeEach
    void setUp() {
        authenticator = mock(Authenticator.class);
        forward = mock(BiConsumer.class);
        lobby = new Lobby(authenticator, PRINCIPAL, forward);
    }

    @Test
    @DisplayName("Should close bad authenticated socket")
    void closeBadSocket() {
        final WebSocket webSocket = mock(WebSocket.class);

        lobby.authenticate(new DefaultWebSocketMessage<>(webSocket, "bad_authentication", null));

        final InOrder inOrder = inOrder(webSocket);
        inOrder.verify(webSocket).send("ERROR wrong authentication");
        inOrder.verify(webSocket).close();
        verify(authenticator).authenticate("bad_authentication");
        verifyZeroInteractions(forward);
    }

    @Test
    @DisplayName("Should close bad authenticated socket")
    void forwardGoodSocket() {
        final WebSocket webSocket = mock(WebSocket.class);
        final MutableWebSocketActions mutableWebSocketActions = mock(MutableWebSocketActions.class);

        when(authenticator.authenticate(any())).thenReturn(PRINCIPAL);

        lobby.authenticate(new DefaultWebSocketMessage<>(webSocket, "good_authentication", mutableWebSocketActions));

        verify(authenticator).authenticate("good_authentication");
        verify(webSocket).send("INFO server authentication succeeded");
        verify(forward).accept(webSocket, mutableWebSocketActions);
        verifyNoMoreInteractions(forward);
    }
}
