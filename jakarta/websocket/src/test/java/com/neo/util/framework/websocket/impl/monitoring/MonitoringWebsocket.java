package com.neo.util.framework.websocket.impl.monitoring;

import com.neo.util.framework.websocket.api.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.api.WebsocketStateContext;
import com.neo.util.framework.websocket.impl.BasicWebsocket;
import com.neo.util.framework.websocket.impl.WebsocketUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NeoUtilWebsocket(monitored = true)
@ApplicationScoped
@ServerEndpoint(value = "/monitoring/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class MonitoringWebsocket implements BasicWebsocket {

    private Map<String, WebsocketStateContext> websocketStateHolderMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        websocketStateHolderMap.put(session.getId(), WebsocketUtil.getStateHoldet(endpointConfig));
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        websocketStateHolderMap.get(session.getId()).broadcastAsync(message);
    }

    @OnClose
    public void onClose(Session session) {

    }
}