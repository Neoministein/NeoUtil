package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractWebsocketEndpoint {

    protected Map<Session, RequestDetails> requestDetailsMap = new ConcurrentHashMap<>();

    @Inject
    protected WebsocketAccessController websocketAccessController;

    @Inject
    protected RequestContextExecutor executor;

    protected void onOpen(Session session) throws IOException {}

    protected void onMessage(Session session, String message) throws IOException {}

    protected void onClose(Session session) throws IOException {}

    protected boolean secured() {
        return false;
    }

    protected Set<String> requiredRoles() {
        return Set.of();
    }

    @OnOpen
    public void setupContext(Session session, EndpointConfig config) throws IOException {
        UserRequestDetails requestDetails = websocketAccessController.createUserRequestDetails(session);
        requestDetailsMap.put(session, requestDetails);

        executor.executeChecked(requestDetails, () -> {
            boolean shouldDisconnect = websocketAccessController.authenticate(
                    requestDetails,
                    getStoredObject(config, HttpHeaders.class.getSimpleName()),
                    requiredRoles());

            if (shouldDisconnect && secured()) {
                session.close();
            } else {
                onOpen(session);
            }
        });
    }

    @OnMessage
    public void preMessageSetup(Session session, String message) throws IOException {
        executor.executeChecked(requestDetailsMap.get(session), () -> onMessage(session, message));
    }

    @OnClose
    public void preCloseSetup(Session session) throws IOException {
        executor.executeChecked(requestDetailsMap.get(session), () -> onClose(session));
    }

    protected String getPathParameter(Session session, String name) {
        return session.getPathParameters().get(name);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getStoredObject(EndpointConfig config, String key) {
        return (T) config.getUserProperties().get(key);
    }
}
