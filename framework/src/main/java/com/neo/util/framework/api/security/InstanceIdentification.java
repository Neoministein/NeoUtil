package com.neo.util.framework.api.security;

/**
 * This interfaces enables the system to get a unique identifier for the running system
 */
public interface InstanceIdentification {

    String MDC_INSTANCE = "instance";

    /**
     * The id of the current running instance
     */
    String getInstanceId();
}
