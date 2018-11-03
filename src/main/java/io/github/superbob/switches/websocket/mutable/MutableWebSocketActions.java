package io.github.superbob.switches.websocket.mutable;

import ratpack.func.Action;
import ratpack.websocket.WebSocketClose;
import ratpack.websocket.WebSocketMessage;

/**
 * Mutable holder of actions defined in {@link ratpack.websocket.WebSocketSpec}.
 * @see ratpack.websocket.WebSocketSpec
 */
public class MutableWebSocketActions {
    private Action<WebSocketClose<MutableWebSocketActions>> closeAction = Action.noop();
    private Action<WebSocketMessage<MutableWebSocketActions>> messageAction = Action.noop();

    public Action<WebSocketClose<MutableWebSocketActions>> getCloseAction() {
        return closeAction;
    }

    /**
     * Mutable counterpart of {@link ratpack.websocket.WebSocketSpec#onClose(Action)}.
     * @param closeAction
     * @return
     * @see ratpack.websocket.WebSocketSpec#onClose(Action)
     */
    public MutableWebSocketActions onClose(Action<WebSocketClose<MutableWebSocketActions>> closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    public Action<WebSocketMessage<MutableWebSocketActions>> getMessageAction() {
        return messageAction;
    }

    /**
     * Mutable counterpart of {@link ratpack.websocket.WebSocketSpec#onMessage(Action)}.
     * @param messageAction
     * @return
     * @see ratpack.websocket.WebSocketSpec#onMessage(Action)
     */
    public MutableWebSocketActions onMessage(Action<WebSocketMessage<MutableWebSocketActions>> messageAction) {
        this.messageAction = messageAction;
        return this;
    }
}
