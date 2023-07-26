package com.neo.util.framework.api.security;

/**
 * This interfaces enables the system to get a unique identifier for the running system
 */
public interface InstanceIdentification {

    String MDC_INSTANCE = "instance";

    /**
     * A unique id of the current running instance.
     * <p>
     * This will method will be called to create an instance {@link com.neo.util.framework.api.request.RequestDetails}.
     * Therefore {@link jakarta.enterprise.context.RequestScoped} will most likely not be establishes yet.
     */
    String getInstanceId();
}
