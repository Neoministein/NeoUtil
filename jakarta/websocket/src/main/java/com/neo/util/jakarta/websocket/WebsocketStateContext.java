package com.neo.util.jakarta.websocket;

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
     * A new Instance of a request detail based for the current context
     */
    WebsocketRequestDetails newRequestDetailInstance();

    /**
     * Broadcasts the message on the async channel and logs the outgoing message length
     *
     * @param message to broadcast
     */
    default void broadcastAsync(String message) {
        getSession().getAsyncRemote().sendText(message);
    }

    /**
     * Broadcasts the message on the async channel and logs the outgoing message length
     *
     * @param message to broadcast
     * @throws IOException if there is a problem delivering the message.
     */
    default void broadcast(String message) throws IOException {
        getSession().getBasicRemote().sendText(message);
    }
}
