package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.websocket.api.WebsocketStateContext;
import jakarta.websocket.Session;

public final class WebsocketUtil {

    private WebsocketUtil() {}

    @SuppressWarnings("unchecked")
    public static <T> T getStoredObject(Session session, String key) {
        return (T) session.getUserProperties().get(key);
    }

    public static void storeWebsocketContext(Session session, WebsocketStateContext websocketStateContext) {
        session.getUserProperties().put(WebsocketStateContext.class.getSimpleName(), websocketStateContext);
    }

    public static WebsocketStateContext getWebsocketContext(Session session) {
        return getStoredObject(session, WebsocketStateContext.class.getSimpleName());
    }

}
