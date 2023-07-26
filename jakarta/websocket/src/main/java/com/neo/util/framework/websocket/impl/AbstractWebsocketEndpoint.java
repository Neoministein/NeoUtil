package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.inject.Inject;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;

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
        UserRequestDetails requestDetails = websocketAccessController.createUserRequestDetails(session);
        storedRequestDetails(config, requestDetails);

        executor.executeChecked(requestDetails, () -> {
            boolean shouldDisconnect = websocketAccessController.authenticate(
                    requestDetails,
                    getStoredObject(config, HttpHeaders.class.getSimpleName()),
                    secured(),
                    requiredRoles());

            if (shouldDisconnect) {
                session.close();
            } else {
                onOpen(session);
            }
        });
    }

    @OnMessage
    public void preMessageSetup(Session session, EndpointConfig config, String message) {
        executor.execute(getStoredRequestDetails(config), () -> onMessage(session, message));
    }

    protected String getPathParameter(Session session, String name) {
        return session.getPathParameters().get(name);
    }

    protected void storedRequestDetails(EndpointConfig config, UserRequestDetails requestDetails) {
        config.getUserProperties().put(RequestDetails.class.getSimpleName(), requestDetails);
    }

    protected UserRequestDetails getStoredRequestDetails(EndpointConfig config) {
        return getStoredObject(config, RequestDetails.class.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    protected <T> T getStoredObject(EndpointConfig config, String key) {
        return (T) config.getUserProperties().get(key);
    }
}
