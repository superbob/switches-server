package io.github.superbob.switches;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import io.github.superbob.switches.authentication.Authenticator;
import io.github.superbob.switches.scheduling.SchedulerService;
import io.github.superbob.switches.websocket.Lobby;
import io.github.superbob.switches.websocket.WebSocketBridge;
import io.github.superbob.switches.websocket.mutable.MutableWebSockets;
import ratpack.registry.Registry;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;

public class Main {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String AUTHENTICATION_HEADER = "Authentication";

    public static void main(String... args) throws Exception {
        final String audience = getConfigValue("AUDIENCE");
        final String clientPrincipal = getConfigValue("CLIENT_PRINCIPAL");
        final String agentPrincipal = getConfigValue("AGENT_PRINCIPAL");

        final NetHttpTransport transport = new NetHttpTransport.Builder().build();
        final GooglePublicKeysManager googlePublicKeysManager = new GooglePublicKeysManager.Builder(transport, JacksonFactory.getDefaultInstance()).build();
        final GoogleIdTokenVerifier googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(googlePublicKeysManager)
                .setAudience(Collections.singletonList(audience))
                .build();
        final Authenticator authenticator = new Authenticator(googleIdTokenVerifier);
        final WebSocketBridge webSocketBridge = new WebSocketBridge();
        final Lobby clientLobby = new Lobby(authenticator, clientPrincipal, webSocketBridge.getClientEndPoint()::attach);
        final Lobby agentLobby = new Lobby(authenticator, agentPrincipal, webSocketBridge.getAgentEndPoint()::attach);
        final SchedulerService schedulerService = new SchedulerService(
                webSocketBridge::pingWebSockets, 0, 30, TimeUnit.SECONDS);

        RatpackServer.start(server -> server
                .serverConfig(c -> c.baseDir(BaseDir.find()))
                .registryOf(r -> r.add(schedulerService))
                .handlers(c -> c
                        .files(f -> f.dir("dist").indexFiles("index.html"))
                        .prefix("api", api -> api
                                .all(ctx -> ctx.next(Registry.single(authenticator.authenticate(ctx.getRequest().getHeaders().get(AUTHENTICATION_HEADER)))))
                                .get("ping", ctx -> ctx.render("{\"message\": \"pong\"}"))
                                .get("status", ctx -> ctx.render(printStatus(webSocketBridge)))
                                .get(ctx -> ctx.render("Hello " + ctx.get(String.class) + "!")))
                        .get("ws/client", ctx -> MutableWebSockets.websocketConnect(
                                ctx, a -> {
                                    LOGGER.debug("Client WebSocket connecting");
                                    a.onMessage(clientLobby::authenticate);
                                }))
                        .get("ws/agent", ctx -> MutableWebSockets.websocketConnect(
                                ctx, a -> {
                                    LOGGER.debug("Agent WebSocket connecting");
                                    a.onMessage(agentLobby::authenticate);
                                }))));
    }

    private static String getConfigValue(String audience) {
        final String value = System.getenv(audience);
        if (value == null || value.length() == 0) {
            throw new MissingConfigurationException("Missing \"" + audience + "\" configuration");
        }
        return value;
    }

    private static String printStatus(WebSocketBridge webSocketBridge) {
        String clientStatus = webSocketBridge.getClientEndPoint().isConnected() ? "connected" : "not_connected";
        String agentStatus = webSocketBridge.getAgentEndPoint().isConnected() ? "connected" : "not_connected";
        return "{\"client\": \"" + clientStatus + "\", \"agent\": \"" + agentStatus + "\"}";
    }
}
