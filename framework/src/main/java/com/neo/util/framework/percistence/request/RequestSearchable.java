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

    protected Instant timestamp;
    protected String requestId;
    protected String initiator;
    protected String context;
    protected String contextType;
    protected boolean failed;
    protected long processTime;

    public RequestSearchable(RequestDetails requestDetails, boolean failed) {
        this.timestamp = requestDetails.getRequestStartDate();
        this.requestId = Long.toString(requestDetails.getRequestId());
        this.initiator = requestDetails.getInitiator();
        this.context = requestDetails.getRequestContext().toString();
        this.contextType = requestDetails.getRequestContext().type();
        this.processTime = System.currentTimeMillis() - requestDetails.getRequestStartDate().toEpochMilli();
        this.failed = failed;
    }

    protected RequestSearchable() {
        //Required by Jackson
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getContext() {
        return context;
    }

    public String getContextType() {
        return contextType;
    }

    public boolean isFailed() {
        return failed;
    }

    public long getProcessTime() {
        return processTime;
    }
}