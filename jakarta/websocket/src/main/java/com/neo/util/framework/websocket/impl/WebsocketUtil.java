package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.websocket.api.WebsocketStateContext;
import jakarta.websocket.EndpointConfig;

public final class WebsocketUtil {

    private WebsocketUtil() {}

    @SuppressWarnings("unchecked")
    public static <T> T getStoredObject(EndpointConfig config, String key) {
        return (T) config.getUserProperties().get(key);
    }

    public static void storeStateHolder(EndpointConfig config, WebsocketStateContext websocketStateHolder) {
        config.getUserProperties().put(WebsocketStateContext.class.getName(), websocketStateHolder);
    }

    public static WebsocketStateContext getStateHoldet(EndpointConfig config) {
        return getStoredObject(config, WebsocketStateContext.class.getName());
    }

}
