package com.neo.util.framework.websocket;

import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.impl.AbstractWebsocketEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@ServerEndpoint(value = "/websocket/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class SocketWithId extends AbstractWebsocketEndpoint {

    protected Map<String, String> messageMap = new HashMap<>();

    @Override
    protected boolean secured() {
        return true;
    }

    @Override
    public void onOpen(Session session) throws IOException{
        System.out.println(session);
        session.getBasicRemote().sendText("pong");
    }

    @Override
    public void onMessage(Session session, String message) {
        String id = getPathParameter(session, "id");
        messageMap.put(id, message);
    }

    public Map<String, String> getMessageMap() {
        return messageMap;
    }
}