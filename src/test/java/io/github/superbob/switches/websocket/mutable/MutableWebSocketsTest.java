package io.github.superbob.switches.websocket.mutable;

import io.github.superbob.switches.websocket.client.TestWebSocketClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ratpack.test.embed.EmbeddedApp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class MutableWebSocketsTest {
    @Test
    @DisplayName("Mutable websocket should be able to mutate the onMessage action handler")
    void mutateOnMessageAction() throws InterruptedException, TimeoutException, ExecutionException {
        final CompletableFuture<String> firstFuture = new CompletableFuture<>();
        final CompletableFuture<String> secondFuture = new CompletableFuture<>();

        try (EmbeddedApp app = EmbeddedApp.fromHandler(ctx -> MutableWebSockets.websocketConnect(
                ctx, a -> a.onMessage(firstMessage -> {
                    firstMessage.getOpenResult().onMessage(
                            secondMessage -> secondFuture.complete(secondMessage.getText()));
                    firstFuture.complete(firstMessage.getText());
                })))) {
            final TestWebSocketClient testWebSocketClient = new TestWebSocketClient(app.getAddress());
            if (!testWebSocketClient.connectBlocking(5, TimeUnit.SECONDS)) {
                fail("Timeout");
            }
            testWebSocketClient.send("First message");
            final String firstActionReceivedMessage = firstFuture.get(5, TimeUnit.SECONDS);
            testWebSocketClient.send("Second message");
            final String secondActionReceivedMessage = secondFuture.get(5, TimeUnit.SECONDS);

            assertThat(firstActionReceivedMessage).isEqualTo("First message");
            assertThat(secondActionReceivedMessage).isEqualTo("Second message");
        }
    }

}
