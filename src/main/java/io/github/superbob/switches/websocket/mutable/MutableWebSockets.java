package io.github.superbob.switches.websocket.mutable;

import ratpack.handling.Context;
import ratpack.websocket.WebSockets;

import java.util.function.Consumer;

/**
 * Utility class used to configure a WebSocket with mutable actions.
 */
public class MutableWebSockets {
    /**
     * Configures a WebSocket with mutable actions using {@link MutableWebSocketActions}.
     * The actions are returned in the open action and are then available in onMessage and onClose handler methods.
     * @param ctx Handler context
     * @param connectConfigurator configurator callback called on connect used to set initial onMessage and/or
     *                            onClose methods.
     * @throws Exception in case of failure in {@link ratpack.websocket.WebSocketConnector#connect}.
     */
    public static void websocketConnect(Context ctx, Consumer<MutableWebSocketActions> connectConfigurator)
            throws Exception {
        final MutableWebSocketActions webSocketActions = new MutableWebSocketActions();
        WebSockets
                .websocket(ctx, ws -> webSocketActions)
                .connect(ws -> {
                    connectConfigurator.accept(webSocketActions);
                    ws
                            .onClose(wsc -> webSocketActions.getCloseAction().execute(wsc))
                            .onMessage(wsc -> webSocketActions.getMessageAction().execute(wsc));
                });
    }
}
