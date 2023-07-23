package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.inject.Inject;
import jakarta.websocket.*;

import java.io.IOException;
import java.util.Set;

public abstract class AbstractWebsocketEndpoint {

    @Inject
    protected WebsocketAccessController websocketAccessController;

    @Inject
    protected RequestContextExecutor executor;

    protected abstract void onOpen(Session session) throws IOException;

    protected abstract void onMessage(Session session, String message);

    protected boolean secured() {
        return false;
    }

    protected Set<String> requiredRoles() {
        return Set.of();
    }

    @OnOpen
    public void setupContext(Session session, EndpointConfig config) throws IOException {
        boolean shouldDisconnect = websocketAccessController.authenticate(session, config, secured(), requiredRoles());

        if (shouldDisconnect) {
            session.close();
        } else {
            onOpen(session);
        }
    }

    @OnMessage
    public void preMessageSetup(Session session, EndpointConfig config, String message) {
        RequestDetails requestDetails = (RequestDetails) config.getUserProperties().get(RequestDetails.class.getSimpleName());
        executor.execute(requestDetails, () -> onMessage(session, message));
    }

    protected String getPathParameter(Session session, String name) {
        return session.getPathParameters().get(name);
    }
}
