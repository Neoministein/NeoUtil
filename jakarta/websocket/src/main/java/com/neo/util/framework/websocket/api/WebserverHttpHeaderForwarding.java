package com.neo.util.framework.websocket.api;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.ws.rs.core.HttpHeaders;

public class WebserverHttpHeaderForwarding extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        sec.getUserProperties().put(HttpHeaders.class.getSimpleName(), request.getHeaders());
    }
}
