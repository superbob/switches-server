package io.github.superbob.switches.websocket;

import io.github.superbob.switches.authentication.Authenticator;
import io.github.superbob.switches.websocket.mutable.MutableWebSocketActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.websocket.WebSocket;
import ratpack.websocket.WebSocketMessage;

import java.util.Objects;
import java.util.function.BiConsumer;

public class Lobby {
    private final static Logger LOGGER = LoggerFactory.getLogger(Lobby.class);

    private final Authenticator authenticator;
    private final String expectedPrincipal;
    private final BiConsumer<WebSocket, MutableWebSocketActions> forward;

    public Lobby(
            Authenticator authenticator,
            String expectedPrincipal,
            BiConsumer<WebSocket, MutableWebSocketActions> forward) {
        this.authenticator = authenticator;
        this.expectedPrincipal = expectedPrincipal;
        this.forward = forward;
    }

    public void authenticate(WebSocketMessage<MutableWebSocketActions> message) {
        final String text = message.getText();
        final WebSocket webSocket = message.getConnection();
        final MutableWebSocketActions webSocketActions = message.getOpenResult();
        LOGGER.debug("Authenticating with the following credentials: {}", text);
        final String principal = authenticator.authenticate(text);
        if (!Objects.equals(principal, expectedPrincipal)) {
            LOGGER.error("Wrong principal: {}", principal);
            webSocket.send("ERROR wrong authentication");
            webSocket.close();
        } else {
            LOGGER.info("Authentication succeeded with principal: {}", principal);
            webSocket.send("INFO server authentication succeeded");
            forward.accept(webSocket, webSocketActions);
        }
    }
}
