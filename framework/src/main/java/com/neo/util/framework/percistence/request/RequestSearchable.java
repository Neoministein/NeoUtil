package com.neo.util.framework.percistence.request;

import com.neo.util.framework.api.persistence.search.AbstractSearchable;
import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.api.request.RequestDetails;

import java.time.Instant;

@SearchableIndex(indexName = RequestSearchable.INDEX_NAME, indexPeriod = IndexPeriod.DAILY)
public class RequestSearchable extends AbstractSearchable implements Searchable {

    public static final String INDEX_NAME = "log-request";

    protected String traceId;
    protected String initiator;
    protected String requestId;
    protected String instanceId;
    protected String context;
    protected String contextType;
    protected Instant timestamp;
    protected boolean failed;
    protected long processTime;

    public RequestSearchable(RequestDetails requestDetails, boolean failed) {
        this.traceId = requestDetails.getTraceId();
        this.initiator = requestDetails.getInitiator();
        this.requestId = Long.toString(requestDetails.getRequestId());
        this.instanceId = requestDetails.getInstanceId();
        this.context = requestDetails.getRequestContext().toString();
        this.contextType = requestDetails.getRequestContext().type();
        this.timestamp = requestDetails.getRequestStartDate();
        this.failed = failed;
        this.processTime = System.currentTimeMillis() - requestDetails.getRequestStartDate().toEpochMilli();
    }

    protected RequestSearchable() {
        //Required by Jackson
    }

    public String getTraceId() {
        return traceId;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getContext() {
        return context;
    }

    public String getContextType() {
        return contextType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isFailed() {
        return failed;
    }

    public long getProcessTime() {
        return processTime;
    }
}