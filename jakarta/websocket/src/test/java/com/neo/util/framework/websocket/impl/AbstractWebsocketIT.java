package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.security.AuthenticationScheme;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.tyrus.client.ClientManager;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class AbstractWebsocketIT {

    @Inject
    protected WebTarget webTarget;

    public Session connectToWebsocket(String path, MessageHandler.Whole<String> messageHandler) {
        return connectToWebsocket(path, null, messageHandler);
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
                if (authHeader != null) {
                    headers.put(HttpHeaders.AUTHORIZATION, List.of(AuthenticationScheme.BEARER + " " + authHeader));
                }
            }
        });
        ClientEndpointConfig clientConfig = configBuilder.build();

        try {
            return client.connectToServer(endpoint, clientConfig, new URI("ws://127.0.0.1:" + webTarget.getUri().getPort() + path));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
