package com.neo.util.framework.websocket;

import com.neo.util.common.impl.ThreadUtils;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@HelidonTest
class WebsocketIT {

    @Inject
    protected WebTarget webTarget;

    @Test
    void test() {
        Session session = connectToWebsocket("websocket/id1", val -> System.out.println(val));
        ThreadUtils.simpleSleep(10000);
        Session session2 = connectToWebsocket("websocket/id2", val -> System.out.println(val));
        ThreadUtils.simpleSleep(1000);
    }

    public Session connectToWebsocket(String path, MessageHandler.Whole<String> messageHandler) {
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
                headers.put(HttpHeaders.AUTHORIZATION, List.of("IDC"));
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
