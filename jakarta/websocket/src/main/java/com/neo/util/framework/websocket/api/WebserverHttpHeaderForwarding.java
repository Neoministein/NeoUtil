package com.neo.util.framework.websocket.api;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.ws.rs.core.AbstractMultivaluedMap;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

public class WebserverHttpHeaderForwarding extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        MultivaluedMap<String, String> headers = new AbstractMultivaluedMap<>(request.getHeaders()) {};
        sec.getUserProperties().put(HttpHeaders.class.getSimpleName(), headers);
    }
}
