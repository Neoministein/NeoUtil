package com.neo.util.framework.websocket.api;

import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.websocket.Session;

import java.io.IOException;

/**
 * Holds the state of an open Websocket
 */
public interface WebsocketStateContext {

    /**
     * The websocket session
     */
    Session getSession();

    /**
     * If the current Websocket is monitored {@link NeoUtilWebsocket#monitored()}
     */
    boolean isMonitored();

    /**
     * A new Instance of a request detail based for the current context
     */
    WebsocketRequestDetails newRequestDetailInstance();

    /**
     * Create a new {@link SocketLogSearchable} and returns the old one
     */
    SocketLogSearchable clearSocketLog();

    /**
     * The current {@link SocketLogSearchable}
     */
    SocketLogSearchable getSocketLogSearchable();

    /**
     * Broadcasts the message on the async channel and logs the outgoing message length
     *
     * @param message to broadcast
     */
    default void broadcastAsync(String message) {
        if (isMonitored()) {
            getSocketLogSearchable().addToOutgoing(message.length());
        }

        getSession().getAsyncRemote().sendText(message);
    }

    /**
     * Broadcasts the message on the async channel and logs the outgoing message length
     *
     * @param message to broadcast
     * @throws IOException if there is a problem delivering the message.
     */
    default void broadcast(String message) throws IOException {
        if (isMonitored()) {
            getSocketLogSearchable().addToOutgoing(message.length());
        }

        getSession().getBasicRemote().sendText(message);
    }
}
