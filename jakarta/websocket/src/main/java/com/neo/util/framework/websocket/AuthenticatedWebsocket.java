package com.neo.util.framework.websocket;

import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.api.scope.NeoUtilWebsocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NeoUtilWebsocket
@ApplicationScoped
@ServerEndpoint(value = "/auth/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class AuthenticatedWebsocket {

    protected Map<String, String> messageMap = new HashMap<>();

    protected Set<String> roles = new HashSet<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig, @PathParam("id") String id) {
        System.out.println(session);
    }

    @OnMessage
    public void onMessage(@PathParam("id") String id, Session session, String message) {
        messageMap.put(id, message);
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println(session);
    }

    public Map<String, String> getMessageMap() {
        return messageMap;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}