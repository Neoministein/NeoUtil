package com.neo.util.framework.websocket;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.ws.rs.core.HttpHeaders;

public class CustomServerEndpointConfig extends ServerEndpointConfig.Configurator {


    public CustomServerEndpointConfig() {
        System.out.println("0: CustomServerEndpointConfig");
    }

    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        sec.getUserProperties().put(HttpHeaders.AUTHORIZATION, request.getHeaders().get(HttpHeaders.AUTHORIZATION));
    }
}
