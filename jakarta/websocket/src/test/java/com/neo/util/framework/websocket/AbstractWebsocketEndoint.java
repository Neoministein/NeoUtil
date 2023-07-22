package com.neo.util.framework.websocket;

import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.inject.Inject;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

import java.util.List;

public abstract class AbstractWebsocketEndoint extends Endpoint {

    @Inject
    protected WebsocketAccessController websocketAccessController;

    @Inject
    protected RequestContextExecutor executor;

    protected abstract boolean secured();

    protected abstract List<String> requedRoles();

    public void onOpen(Session session, EndpointConfig config) {

    }

    protected String getPathParameter(Session session, String name) {
        return session.getPathParameters().get(name);
    }
}
