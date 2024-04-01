package com.neo.util.framework.websocket;

import com.neo.util.framework.websocket.api.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.api.WebsocketStateHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@NeoUtilWebsocket(monitored = true)
@ApplicationScoped
@ServerEndpoint(value = "/monitoring", configurator = WebserverHttpHeaderForwarding.class)
public class MonitoringWebsocket {


    @Inject
    protected Instance<WebsocketStateHolder> websocketStateHolder;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    @OnMessage
    public void onMessage(Session session, String message) {
        for (WebsocketStateHolder stateHolder: websocketStateHolder) {
            stateHolder.broadcast(message);
        }
    }

    @OnClose
    public void onClose(Session session) {

    }
}