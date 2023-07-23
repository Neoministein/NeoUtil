package com.neo.util.framework.websocket;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.framework.api.security.AuthenticationScheme;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@HelidonTest
class WebsocketIT {

    @Inject
    protected SocketWithId socketWithId;

    @Inject
    protected WebTarget webTarget;

    @Test
    void test() throws IOException {
        Session session = connectToWebsocket("websocket/id1", BasicAuthorizationProvider.NORMAL_TOKEN,val -> {});
        session.getBasicRemote().sendText("A message 1");
        ThreadUtils.simpleSleep(1000);
        Map<String, String> map = socketWithId.getMessageMap();

        Session session2 = connectToWebsocket("websocket/id2", "", val -> System.out.println(val));
        session.getBasicRemote().sendText("A message 2");
        ThreadUtils.simpleSleep(1000);
        map = socketWithId.getMessageMap();
        session.getBasicRemote().sendText("A message");
        Session session3 = connectToWebsocket("websocket/id3", BasicAuthorizationProvider.NORMAL_TOKEN,val -> {});
        session3.getBasicRemote().sendText("A message 3");
        ThreadUtils.simpleSleep(1000);
        map = socketWithId.getMessageMap();
        System.out.println();
    }

    public Session connectToWebsocket(String path, String authHeader, MessageHandler.Whole<String> messageHandler) {
        Endpoint endpoint = new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                session.addMessageHandler(messageHandler);
            }
        };

        ClientManager client = ClientManager.createClient();

        ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
        configBuilder.configurator(new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put(HttpHeaders.AUTHORIZATION, List.of(AuthenticationScheme.BEARER + " " + authHeader));
            }
        });
        ClientEndpointConfig clientConfig = configBuilder.build();

        try {
            return client.connectToServer(endpoint, clientConfig, new URI("ws://127.0.0.1:" + webTarget.getUri().getPort() + "/" + path));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
