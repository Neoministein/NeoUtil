package com.neo.util.framework.percistence.request;

import com.neo.util.framework.api.persistence.search.AbstractSearchable;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.request.RequestDetails;

import java.time.Instant;

public abstract class AbstractLogSearchable extends AbstractSearchable implements Searchable {

    public static final String INDEX_PREFIX = "log";

    protected String traceId;
    protected String requestId;
    protected String instanceId;
    protected String contextType;
    protected Instant timestamp;

    protected AbstractLogSearchable(RequestDetails requestDetails) {
        this.traceId = requestDetails.getTraceId();
        this.requestId = Long.toString(requestDetails.getRequestId());
        this.instanceId = requestDetails.getInstanceId();
        this.contextType = requestDetails.getRequestContext().type();
        this.timestamp = requestDetails.getRequestStartDate();
    }

    protected AbstractLogSearchable(String traceId, String requestId, String instanceId, String contextType, Instant timestamp) {
        this.traceId = traceId;
        this.requestId = requestId;
        this.instanceId = instanceId;
        this.contextType = contextType;
        this.timestamp = timestamp;
    }

    protected AbstractLogSearchable() {
        //Required by Jackson
    }

    public String getTraceId() {
        return traceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getContextType() {
        return contextType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}