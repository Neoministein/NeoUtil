package com.neo.util.framework.websocket.api;

import jakarta.interceptor.InvocationContext;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

import java.util.Collection;

/**
 * A CDI in order to {@link jakarta.enterprise.inject.Specializes} the logic of the Interceptors
 */
public interface WebsocketInterceptorLogic {

    /**
     * Handles the {@link jakarta.websocket.OnOpen} logic
     *
     * @param invocationContext the current invocationContext
     * @param session the active websocket session
     * @param config the EndpointConfig
     *
     * @throws Exception due to {@link InvocationContext#proceed()}
     */
    void onOpen(InvocationContext invocationContext, Session session, EndpointConfig config) throws Exception;

    /**
     * Handles the {@link jakarta.websocket.OnMessage} logic
     *
     * @param invocationContext the current invocationContext
     * @param session the active websocket session
     *
     * @throws Exception due to {@link InvocationContext#proceed()}
     */
    void onMessage(InvocationContext invocationContext, Session session) throws Exception;

    /**
     * Handles the {@link jakarta.websocket.OnClose} logic
     *
     * @param invocationContext the current invocationContext
     * @param session the active websocket session
     *
     * @throws Exception due to {@link InvocationContext#proceed()}
     */
    void onClose(InvocationContext invocationContext, Session session) throws Exception;

    /**
     * Returns all active {@link WebsocketStateContext}
     */
    Collection<WebsocketStateContext> getActiveWebsocketStates();
}
