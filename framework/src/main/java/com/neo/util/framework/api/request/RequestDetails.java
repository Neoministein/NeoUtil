package com.neo.util.framework.api.request;

import java.time.Instant;

/**
 * This interface consolidates all the data based on this request.
 */
public interface RequestDetails {

    /**
     * The logging name for MDC
     */
    String TRACE_ID = "traceId";

    /**
     * Returns the initiator of the request
     */
    String getInitiator();

    long getRequestId();

    /**
     * A unique identifier for the current request
     */
    String getRequestIdentification();

    /**
     * The current context of the request
     */
    RequestContext getRequestContext();

    /**
     * Returns the date the request has been started
     */
    Instant getRequestStartDate();
}
