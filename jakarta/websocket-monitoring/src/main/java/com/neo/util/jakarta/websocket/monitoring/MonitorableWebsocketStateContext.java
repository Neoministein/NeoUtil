package com.neo.util.jakarta.websocket.monitoring;

import com.neo.util.jakarta.websocket.DefaultWebsocketStateContext;
import com.neo.util.jakarta.websocket.WebsocketRequestDetails;
import com.neo.util.jakarta.websocket.WebsocketStateContext;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.Session;

import java.io.IOException;
import java.util.function.Function;

public class MonitorableWebsocketStateContext implements WebsocketStateContext {

    protected final DefaultWebsocketStateContext interceptorWebsocketStateContext;
    protected final Function<InvocationContext, String> messageFunc;
    protected SocketLogSearchable socketLogSearchable;

    public MonitorableWebsocketStateContext(DefaultWebsocketStateContext websocketStateContext, Function<InvocationContext, String> messageFunc) {
        this.interceptorWebsocketStateContext = websocketStateContext;
        this.messageFunc = messageFunc;
        this.socketLogSearchable = new SocketLogSearchable(interceptorWebsocketStateContext.requestDetails());
    }

    @Override
    public Session getSession() {
        return interceptorWebsocketStateContext.getSession();
    }

    @Override
    public WebsocketRequestDetails newRequestDetailInstance() {
        return interceptorWebsocketStateContext.newRequestDetailInstance();
    }

    @Override
    public void broadcastAsync(String message) {
        getSocketLogSearchable().addToOutgoing(message.length());
        getSession().getAsyncRemote().sendText(message);
    }

    @Override
    public void broadcast(String message) throws IOException {
        getSocketLogSearchable().addToOutgoing(message.length());
        getSession().getBasicRemote().sendText(message);
    }

    public void addToIncomingSocketLog(InvocationContext context) {
        socketLogSearchable.addToIncoming(messageFunc.apply(context).length());
    }

    /**
     * Create a new {@link SocketLogSearchable} and returns the old one
     */
    public SocketLogSearchable clearSocketLog() {
        SocketLogSearchable temp = this.socketLogSearchable;
        this.socketLogSearchable = new SocketLogSearchable(interceptorWebsocketStateContext.requestDetails());
        return temp;
    }

    public SocketLogSearchable getSocketLogSearchable() {
        return socketLogSearchable;
    }
}
