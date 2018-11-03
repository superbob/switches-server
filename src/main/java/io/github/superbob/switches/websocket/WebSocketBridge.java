package io.github.superbob.switches.websocket;

import io.github.superbob.switches.websocket.mutable.MutableWebSocketActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.websocket.WebSocket;
import ratpack.websocket.WebSocketMessage;

import java.util.Objects;
import java.util.function.Supplier;

public class WebSocketBridge {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketBridge.class);

    private final EndPoint clientEndPoint;
    private final EndPoint agentEndPoint;

    public WebSocketBridge() {
        clientEndPoint = new EndPoint(this::getAgentEndPoint);
        agentEndPoint = new EndPoint(this::getClientEndPoint);
    }

    public EndPoint getClientEndPoint() {
        return clientEndPoint;
    }

    public EndPoint getAgentEndPoint() {
        return agentEndPoint;
    }

    public void pingWebSockets() {
        pingConnected(agentEndPoint);
        pingConnected(clientEndPoint);
    }

    private void pingConnected(EndPoint endPoint)
    {
        if (endPoint.isConnected()) {
            endPoint.webSocket.send("PING");
        }
    }

    public static class EndPoint {
        private WebSocket webSocket = null;
        private final Supplier<EndPoint> otherEndSupplier;

        public EndPoint(Supplier<EndPoint> otherEndSupplier) {
            this.otherEndSupplier = otherEndSupplier;
        }

        public void attach(WebSocket webSocket, MutableWebSocketActions webSocketActions) {
            if (this.webSocket != null) {
                LOGGER.info("Closing previous WebSocket");
                this.webSocket.close();
            }
            LOGGER.info("Attaching WebSocket to EndPoint");
            this.webSocket = webSocket;
            webSocketActions.onMessage(this::receiveMessage);
            webSocketActions.onClose(wsc -> this.detach());
        }

        public void detach() {
            LOGGER.info("Detaching WebSocket from EndPoint");
            this.webSocket = null;
        }

        public void receiveMessage(final WebSocketMessage<?> message) {
            final EndPoint otherEnd = otherEndSupplier.get();
            final String text = message.getText();
            LOGGER.debug("The following message has been received on EndPoint: {}", text);
            if (Objects.equals(text, "PONG")) {
                LOGGER.debug("Received PONG");
            } else if (!otherEnd.isConnected()) {
                LOGGER.error("No client is connected to the other EndPoint");
                webSocket.send("ERROR no client connected");
            } else {
                LOGGER.debug("Forwarding message to other EndPoint");
                otherEnd.webSocket.send(text);
            }
        }

        public boolean isConnected() {
            return webSocket != null;
        }
    }
}
