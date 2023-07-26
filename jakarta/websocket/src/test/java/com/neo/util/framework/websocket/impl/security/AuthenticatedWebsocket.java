package com.neo.util.framework.websocket.impl.security;

import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.impl.AbstractWebsocketEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@ServerEndpoint(value = "/auth/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class AuthenticatedWebsocket extends AbstractWebsocketEndpoint {

    protected Map<String, String> messageMap = new HashMap<>();

    protected Set<String> roles = new HashSet<>();

    @Override
    protected boolean secured() {
        return true;
    }

    @Override
    protected Set<String> requiredRoles() {
        return roles;
    }

    @Override
    public void onOpen(Session session) throws IOException{
        System.out.println(session);
    }

    @Override
    public void onMessage(Session session, String message) {
        String id = getPathParameter(session, "id");
        messageMap.put(id, message);
    }

    public Map<String, String> getMessageMap() {
        return messageMap;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}