package com.neo.util.framework.websocket.api;

import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.websocket.Session;

public interface WebsocketStateHolder {

    boolean isMonitored();

    Session getSession();

    WebsocketRequestDetails getRequestDetails();

    SocketLogSearchable clearSocketLog();

    SocketLogSearchable getSocketLogSearchable();

    default void broadcast(String message) {
        if (isMonitored()) {
            getSocketLogSearchable().addToOutgoing(message.length());
        }

        getSession().getAsyncRemote().sendText(message);
    }
}
