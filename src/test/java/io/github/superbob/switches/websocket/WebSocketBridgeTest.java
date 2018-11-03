package io.github.superbob.switches.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.superbob.switches.websocket.mutable.MutableWebSocketActions;
import ratpack.websocket.WebSocket;

class WebSocketBridgeTest  {
    private WebSocket clientWebSocket;
    private MutableWebSocketActions clientMutableWebSocketActions;
    private WebSocket agentWebSocket;
    private MutableWebSocketActions agentMutableWebSocketActions;
    private WebSocketBridge webSocketBridge;

    @BeforeEach
    void setUp() {
        clientWebSocket = mock(WebSocket.class);
        clientMutableWebSocketActions = mock(MutableWebSocketActions.class);
        agentWebSocket = mock(WebSocket.class);
        agentMutableWebSocketActions = mock(MutableWebSocketActions.class);
        webSocketBridge = new WebSocketBridge();
    }

    @DisplayName("Should send ping to agent when it is connected")
    @Test
    void pingAgent() {
        webSocketBridge.getAgentEndPoint().attach(agentWebSocket, agentMutableWebSocketActions);
        webSocketBridge.pingWebSockets();

        verify(agentWebSocket).send("PING");
        verify(agentMutableWebSocketActions).onMessage(any());
        verify(agentMutableWebSocketActions).onClose(any());
        verifyNoMoreInteractions(agentWebSocket, agentMutableWebSocketActions);
	}

    @DisplayName("Should send ping to client when it is connected")
    @Test
    void pingClient() {
        webSocketBridge.getClientEndPoint().attach(clientWebSocket, clientMutableWebSocketActions);

        webSocketBridge.pingWebSockets();

        verify(clientWebSocket).send("PING");
        verify(clientMutableWebSocketActions).onMessage(any());
        verify(clientMutableWebSocketActions).onClose(any());
        verifyNoMoreInteractions(clientWebSocket, clientMutableWebSocketActions);
    }

    @DisplayName("Should send ping to agent and client when it is connected")
    @Test
    void pingBoth() {
        webSocketBridge.getAgentEndPoint().attach(agentWebSocket, agentMutableWebSocketActions);
        webSocketBridge.getClientEndPoint().attach(clientWebSocket, clientMutableWebSocketActions);

        webSocketBridge.pingWebSockets();

        verify(clientWebSocket).send("PING");
        verify(clientMutableWebSocketActions).onMessage(any());
        verify(clientMutableWebSocketActions).onClose(any());
        verify(agentWebSocket).send("PING");
        verify(agentMutableWebSocketActions).onMessage(any());
        verify(agentMutableWebSocketActions).onClose(any());
        verifyNoMoreInteractions(clientWebSocket, clientMutableWebSocketActions, agentWebSocket, agentMutableWebSocketActions);
	}
}
