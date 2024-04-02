package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import com.neo.util.framework.websocket.api.WebsocketStateContext;
import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.Session;

import java.util.function.Function;

public class InterceptorWebsocketStateHolder implements WebsocketStateContext {

    protected boolean monitored;
    protected Session session;
    protected WebsocketRequestDetails requestDetails;
    protected SocketLogSearchable socketLogSearchable;
    protected Function<InvocationContext, String> messageFunc;

    public InterceptorWebsocketStateHolder(Session session, WebsocketRequestDetails requestDetails, Function<InvocationContext, String> messageFunc, boolean monitored) {
        this.monitored = monitored;
        this.session = session;
        this.requestDetails = requestDetails;
        this.messageFunc = messageFunc;
        this.socketLogSearchable = new SocketLogSearchable(requestDetails);
    }

    @Override
    public boolean isMonitored() {
        return monitored;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public WebsocketRequestDetails newRequestDetailInstance() {
        return requestDetails.newInstance();
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
}
