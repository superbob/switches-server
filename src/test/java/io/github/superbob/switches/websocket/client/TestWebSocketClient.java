package io.github.superbob.switches.websocket.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A test WebSocket client powered by <a href="https://github.com/TooTallNate/Java-WebSocket">Java-WebSocket</a>.
 * Largely Inspired by <a href="https://github.com/ratpack/ratpack/blob/master/ratpack-test-internal/src/main/groovy/ratpack/websocket/RecordingWebSocketClient.groovy">RecordingWebSocketClient.groovy</a>.
 */
public class TestWebSocketClient extends WebSocketClient {
    private ServerHandshake serverHandshake;
    private final Queue<String> messages = new LinkedBlockingQueue<>();
    private int closeCode;
    private String closeReason;
    private boolean closeRemote;
    private CountDownLatch closeLatch;
    private Exception exception;

    public TestWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        serverHandshake = handshakedata;
    }

    @Override
    public void onMessage(String message) {
        messages.add(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        closeCode = code;
        closeReason = reason;
        closeRemote = remote;
        closeLatch.countDown();
    }

    @Override
    public void onError(Exception ex) {
        exception = ex;
    }

    public void waitForClosed() throws InterruptedException {
        if (!closeLatch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("websocket connection did not close");
        }
    }

    public ServerHandshake getServerHandshake() {
        return serverHandshake;
    }

    public Queue<String> getMessages() {
        return messages;
    }

    public int getCloseCode() {
        return closeCode;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public boolean isCloseRemote() {
        return closeRemote;
    }

    public CountDownLatch getCloseLatch() {
        return closeLatch;
    }

    public Exception getException() {
        return exception;
    }
}
