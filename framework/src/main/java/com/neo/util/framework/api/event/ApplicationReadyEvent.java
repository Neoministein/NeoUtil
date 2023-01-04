package com.neo.util.framework.api.event;

/**
 * This class is used for a CDI event which is fired when the application is ready
 */
@SuppressWarnings("java:S1118") // Used for a CDI events
public class ApplicationReadyEvent {

    public static final String EVENT_NAME = "ReadyEvent";
}
