package com.neo.util.jakarta.websocket.monitoring;

import com.neo.util.jakarta.websocket.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NeoUtilWebsocket(monitored = true)
@ApplicationScoped
@ServerEndpoint(value = "/monitoring/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class MonitoringWebsocket implements BasicWebsocket {

    private Map<String, WebsocketStateContext> websocketStateContextMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        websocketStateContextMap.put(session.getId(), WebsocketUtil.getWebsocketContext(session));
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        websocketStateContextMap.get(session.getId()).broadcastAsync(message);
    }

    @OnClose
    public void onClose(Session session) {

    }
}