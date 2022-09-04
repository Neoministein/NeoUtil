package com.neo.util.framework.elastic.impl;

/**
 * Connection status CDI event
 */
public class ElasticSearchConnectionStatusEvent {

    public static final String STATUS_EVENT_CONNECTED = "Connected";
    public static final String STATUS_EVENT_DISCONNECT = "Disconnect";

    String connectionStatus;

    public ElasticSearchConnectionStatusEvent(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

}
