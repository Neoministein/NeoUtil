package com.neo.util.jakarta.websocket.security;

import com.neo.util.jakarta.websocket.BasicWebsocket;
import com.neo.util.jakarta.websocket.NeoUtilWebsocket;
import com.neo.util.jakarta.websocket.WebserverHttpHeaderForwarding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.HashMap;
import java.util.Map;

@NeoUtilWebsocket(secured = true)
@ApplicationScoped
@ServerEndpoint(value = "/auth/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class AuthenticatedWebsocket implements BasicWebsocket {

    protected Map<String, String> messageMap = new HashMap<>();

    public AuthenticatedWebsocket() {
        System.out.println();
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println(session);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("id") String id) {
        messageMap.put(id, message);
    }

    public Map<String, String> getMessageMap() {
        return messageMap;
    }
}