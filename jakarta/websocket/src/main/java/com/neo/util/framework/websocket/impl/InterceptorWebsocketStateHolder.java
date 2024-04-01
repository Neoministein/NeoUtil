package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import com.neo.util.framework.websocket.api.WebsocketScope;
import com.neo.util.framework.websocket.api.WebsocketStateHolder;
import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.Session;

import java.util.function.Function;

@WebsocketScope
public class InterceptorWebsocketStateHolder implements WebsocketStateHolder {

    protected boolean initialized = false;

    protected boolean monitored = false;
    protected Session session = null;
    protected WebsocketRequestDetails requestDetails = null;
    protected SocketLogSearchable socketLogSearchable = null;
    protected Function<InvocationContext, String> messageFunc = null;

    @Override
    public boolean isMonitored() {
        return monitored;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public WebsocketRequestDetails getRequestDetails() {
        return requestDetails;
    }

    @Override
    public SocketLogSearchable clearSocketLog() {
        SocketLogSearchable temp = this.socketLogSearchable;
        this.socketLogSearchable = new SocketLogSearchable(requestDetails);
        return temp;
    }

    public SocketLogSearchable getSocketLogSearchable() {
        return socketLogSearchable;
    }

    public void addToIncomingSocketLog(InvocationContext context) {
        if (monitored) {
            socketLogSearchable.addToIncoming(messageFunc.apply(context).length());
        }
    }

    public void setState(Session session, WebsocketRequestDetails requestDetails, Function<InvocationContext, String> messageFunc, boolean monitored) {
        if (!initialized) {
            this.monitored = monitored;
            this.session = session;
            this.requestDetails = requestDetails;
            this.socketLogSearchable = new SocketLogSearchable(requestDetails);
            this.messageFunc = messageFunc;
        }
    }
}
