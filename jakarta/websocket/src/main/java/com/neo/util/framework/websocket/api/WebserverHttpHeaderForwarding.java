package com.neo.util.framework.websocket.api;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.List;

public class WebserverHttpHeaderForwarding extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        List<String> header = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (header != null && !header.isEmpty()) {
            sec.getUserProperties().put(HttpHeaders.AUTHORIZATION, header.get(0));
        }
    }
}
