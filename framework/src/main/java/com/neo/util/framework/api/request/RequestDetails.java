package com.neo.util.framework.api.request;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This interface consolidates all the data based on this request.
 */
public interface RequestDetails {

    String MDC_REQUEST_ID = "requestId";

    String MDC_REQUEST_CONTEXT_TYPE = "contextType";

    AtomicLong REQUEST_ID = new AtomicLong();

    /**
     * Returns the initiator of the request
     */
    String getInitiator();

    /**
     * An incremental number that counts up for each request based on the {@link RequestContext#type()}
     */
    long getRequestId();

    /**
     * The name of the current instance
     */
    String getInstanceId();

    /**
     * A unique identifier for the current
     */
    default String getFullRequestId() {
        return getInstanceId() + ":" + getRequestContext().type() + ":" + getRequestId();
    }

    /**
     * The current context of the request
     */
    RequestContext getRequestContext();

    /**
     * Returns the date the request has been started
     */
    Instant getRequestStartDate();
}
