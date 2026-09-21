package com.neo.util.jakarta.request;

import com.neo.util.api.request.RequestDetails;

/**
 * This interfaces enables the system to get a unique identifier for the running system
 */
public interface InstanceIdentification {

    String MDC_INSTANCE = "instance";

    /**
     * A unique id of the current running instance.
     * <p>
     * This will method will be called to create an instance {@link RequestDetails}.
     * Therefore {@link jakarta.enterprise.context.RequestScoped} will most likely not be establishes yet.
     */
    String getInstanceId();
}
