package io.github.superbob.switches.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import io.github.superbob.switches.websocket.mutable.MutableWebSocketActions;
import ratpack.websocket.WebSocket;
import ratpack.websocket.internal.DefaultWebSocketMessage;

class EndPointTest {
    private WebSocketBridge.EndPoint endPoint;
    private WebSocketBridge.EndPoint otherEndpoint;

    @BeforeEach
    void setUp() {
        endPoint = new WebSocketBridge.EndPoint(() -> otherEndpoint);
        otherEndpoint = new WebSocketBridge.EndPoint(() -> endPoint);
    }

    @DisplayName("Inital endpoint should not be connected")
    @Test
    void initial_EndPoint_should_not_be_connected() {
        assertThat(endPoint.isConnected()).isFalse();
    }

    @DisplayName("Attached endpoint should be connected")
    @Test
    void attached_EndPoint_should_be_connected() {
        final WebSocket webSocket = mock(WebSocket.class);
        final MutableWebSocketActions mutableWebSocketActions = mock(MutableWebSocketActions.class);

        endPoint.attach(webSocket, mutableWebSocketActions);

        assertThat(endPoint.isConnected()).isTrue();

        verifyZeroInteractions(webSocket);
        verify(mutableWebSocketActions).onMessage(any());
        verify(mutableWebSocketActions).onClose(any());
        verifyNoMoreInteractions(mutableWebSocketActions);
    }

    @DisplayName("Double attached endpoint should close previous one")
    @Test
    void double_attached_EndPoint_should_be_close_previous() {
        final WebSocket firstWebSocket = mock(WebSocket.class);
        final MutableWebSocketActions firstMutableWebSocketActions = mock(MutableWebSocketActions.class);
        final WebSocket secondWebSocket = mock(WebSocket.class);
        final MutableWebSocketActions secondMutableWebSocketActions = mock(MutableWebSocketActions.class);

        endPoint.attach(firstWebSocket, firstMutableWebSocketActions);
        endPoint.attach(secondWebSocket, secondMutableWebSocketActions);

        assertThat(endPoint.isConnected()).isTrue();

        verify(firstWebSocket).close();
        verifyNoMoreInteractions(firstWebSocket);
        verifyZeroInteractions(secondWebSocket);
        final InOrder inOrder = inOrder(firstMutableWebSocketActions, secondMutableWebSocketActions);
        inOrder.verify(firstMutableWebSocketActions).onMessage(any());
        inOrder.verify(firstMutableWebSocketActions).onClose(any());
        inOrder.verify(secondMutableWebSocketActions).onMessage(any());
        inOrder.verify(secondMutableWebSocketActions).onClose(any());
        verifyNoMoreInteractions(firstMutableWebSocketActions, secondMutableWebSocketActions);
    }

    @DisplayName("Detached endpoint should not be connected")
    @Test
    void detached_EndPoint_should_be_not_connected() {
        final WebSocket webSocket = mock(WebSocket.class);
        final MutableWebSocketActions mutableWebSocketActions = mock(MutableWebSocketActions.class);

        endPoint.attach(webSocket, mutableWebSocketActions);
        endPoint.detach();

        assertThat(endPoint.isConnected()).isFalse();

        verifyZeroInteractions(webSocket);
        verify(mutableWebSocketActions).onMessage(any());
        verify(mutableWebSocketActions).onClose(any());
        verifyNoMoreInteractions(mutableWebSocketActions);
    }

    @DisplayName("ReceiveMessage should do nothing when receiving PONG")
    @Test
    void receiveMessage_should_do_nothing_on_pong()
    {
        final WebSocket webSocket = mock(WebSocket.class);
        final MutableWebSocketActions mutableWebSocketActions = mock(MutableWebSocketActions.class);
        final WebSocket otherWebSocket = mock(WebSocket.class);
        final MutableWebSocketActions otherMutableWebSocketActions = mock(MutableWebSocketActions.class);

        endPoint.attach(webSocket, mutableWebSocketActions);
        otherEndpoint.attach(otherWebSocket, otherMutableWebSocketActions);
        endPoint.receiveMessage(new DefaultWebSocketMessage<>(webSocket, "PONG", mutableWebSocketActions));

        verifyZeroInteractions(webSocket, otherWebSocket);
    }

    @DisplayName("ReceiveMessage should reply to sender when other is not connected")
    @Test
    void receiveMessage_should_reply_when_other_is_not_connected()
    {
        final WebSocket webSocket = mock(WebSocket.class);
        final MutableWebSocketActions mutableWebSocketActions = mock(MutableWebSocketActions.class);

        endPoint.attach(webSocket, mutableWebSocketActions);
        endPoint.receiveMessage(new DefaultWebSocketMessage<>(webSocket, "hello", mutableWebSocketActions));

        verify(webSocket).send("ERROR no client connected");
        verifyNoMoreInteractions(webSocket);
    }

    @DisplayName("ReceiveMessage should forward to other when it is connected")
    @Test
    void receiveMessage_should_forward()
    {
        final WebSocket webSocket = mock(WebSocket.class);
        final MutableWebSocketActions mutableWebSocketActions = mock(MutableWebSocketActions.class);
        final WebSocket otherWebSocket = mock(WebSocket.class);
        final MutableWebSocketActions otherMutableWebSocketActions = mock(MutableWebSocketActions.class);

        endPoint.attach(webSocket, mutableWebSocketActions);
        otherEndpoint.attach(otherWebSocket, otherMutableWebSocketActions);
        endPoint.receiveMessage(new DefaultWebSocketMessage<>(webSocket, "hello there", mutableWebSocketActions));

        verifyZeroInteractions(webSocket);
        verify(otherWebSocket).send("hello there");
        verifyNoMoreInteractions(otherWebSocket);
    }

}
