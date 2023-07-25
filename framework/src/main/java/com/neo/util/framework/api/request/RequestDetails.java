package com.neo.util.framework.api.request;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This interface consolidates all the data based on this request.
 */
public interface RequestDetails {

    String MDC_TRACE_ID = "traceId";
    String MDC_REQUEST_ID = "requestId";
    String MDC_REQUEST_CONTEXT_TYPE = "contextType";

    AtomicLong REQUEST_ID = new AtomicLong();

    /**
     * A unique identifier for the current request which will be kept across multiple instances
     */
    String getTraceId();

    /**
     * Returns the initiator of the request
     */
    String getInitiator();

    /**
     * An incremental number that counts up for the current instance
     */
    long getRequestId();

    /**
     * The name of the current instance
     */
    String getInstanceId();

    /**
     * The current context of the request
     */
    RequestContext getRequestContext();

    /**
     * Returns the date the request has been started
     */
    Instant getRequestStartDate();
}
