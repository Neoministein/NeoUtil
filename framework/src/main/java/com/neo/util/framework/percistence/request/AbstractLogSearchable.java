package com.neo.util.framework.percistence.request;

import com.neo.util.framework.api.persistence.search.AbstractSearchable;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.request.RequestDetails;

import java.time.Instant;

public abstract class AbstractLogSearchable extends AbstractSearchable implements Searchable {

    public static final String INDEX_PREFIX = "log";

    protected Instant timestamp;
    protected String instance;
    protected String contextType;
    protected String requestId;
    protected String traceId;

    protected AbstractLogSearchable(RequestDetails requestDetails) {
        this.timestamp = requestDetails.getRequestStartDate();
        this.instance = requestDetails.getInstanceId();
        this.contextType = requestDetails.getRequestContext().type();
        this.requestId = Long.toString(requestDetails.getRequestId());
        this.traceId = requestDetails.getTraceId();
    }

    protected AbstractLogSearchable(Instant timestamp, String instanceId, String contextType, String requestId, String traceId) {
        this.timestamp = timestamp;
        this.instance = instanceId;
        this.contextType = contextType;
        this.requestId = requestId;
        this.traceId = traceId;
    }

    protected AbstractLogSearchable() {
        //Required by Jackson
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getInstance() {
        return instance;
    }

    public String getContextType() {
        return contextType;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTraceId() {
        return traceId;
    }
}