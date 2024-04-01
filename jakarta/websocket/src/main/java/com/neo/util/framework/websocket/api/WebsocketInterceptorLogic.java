package com.neo.util.framework.websocket.api;

import jakarta.interceptor.InvocationContext;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

public interface WebsocketInterceptorLogic {

    void onOpen(InvocationContext invocationContext, Session session, EndpointConfig config) throws Exception;

    void onMessage(InvocationContext invocationContext, Session session) throws Exception;

    void onClose(InvocationContext invocationContext, Session session) throws Exception;
}
