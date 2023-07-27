package com.neo.util.framework.percistence.request;

import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.api.request.RequestDetails;

@SearchableIndex(indexName = RequestLogSearchable.INDEX_NAME, indexPeriod = IndexPeriod.DAILY)
public class RequestLogSearchable extends AbstractLogSearchable implements Searchable {

    public static final String INDEX_NAME = INDEX_PREFIX + "-request";

    protected String initiator;
    protected String context;
    protected boolean failed;
    protected long processTime;

    public RequestLogSearchable(RequestDetails requestDetails, boolean failed) {
        super(requestDetails);
        this.initiator = requestDetails.getInitiator();
        this.context = requestDetails.getRequestContext().toString();
        this.failed = failed;
        this.processTime = System.currentTimeMillis() - requestDetails.getRequestStartDate().toEpochMilli();
    }

    protected RequestLogSearchable() {
        //Required by Jackson
    }

    public String getInitiator() {
        return initiator;
    }

    public String getContext() {
        return context;
    }

    public boolean isFailed() {
        return failed;
    }

    public long getProcessTime() {
        return processTime;
    }
}