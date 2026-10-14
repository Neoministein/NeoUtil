package com.neo.util.jakarta.websocket;

import jakarta.websocket.Session;

public record DefaultWebsocketStateContext(Session session, WebsocketRequestDetails requestDetails) implements WebsocketStateContext {

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public WebsocketRequestDetails newRequestDetailInstance() {
        return requestDetails.newInstance();
    }
}
