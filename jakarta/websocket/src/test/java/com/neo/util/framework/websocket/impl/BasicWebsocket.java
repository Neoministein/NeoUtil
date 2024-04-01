package com.neo.util.framework.websocket.impl;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

public interface BasicWebsocket {

    @OnOpen
    default void onOpen(Session session, EndpointConfig config) {}

    @OnClose
    default void onClose(Session session) {}
}
