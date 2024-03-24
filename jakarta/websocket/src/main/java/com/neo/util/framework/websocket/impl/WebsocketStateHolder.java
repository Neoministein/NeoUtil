package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import com.neo.util.framework.websocket.api.scope.WebsocketScope;
import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.websocket.Session;

@WebsocketScope
public class WebsocketStateHolder {

    protected Session session = null;
    protected WebsocketRequestDetails requestDetails = null;
    protected SocketLogSearchable socketLogSearchable = null;

    public Session getSession() {
        return session;
    }

    public WebsocketRequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setState(Session session, WebsocketRequestDetails requestDetails) {
        if (this.session == null) {
            this.session = session;
        }
        if (this.requestDetails == null) {
            this.requestDetails = requestDetails;
            this.socketLogSearchable = new SocketLogSearchable(requestDetails);
        }
    }
}
