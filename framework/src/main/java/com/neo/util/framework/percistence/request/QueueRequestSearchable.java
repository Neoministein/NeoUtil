package com.neo.util.framework.percistence.request;

import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.impl.request.QueueRequestDetails;

@SearchableIndex(indexName = RequestSearchable.INDEX_NAME, indexPeriod = IndexPeriod.DAILY)
public class QueueRequestSearchable extends RequestSearchable implements Searchable {

    protected String originalRequestId;
    protected String originalInstanceId;

    public QueueRequestSearchable(QueueRequestDetails queueRequestDetails, boolean failed) {
        super(queueRequestDetails, failed);
        this.originalRequestId = Long.toString(queueRequestDetails.getRequestId());
        this.originalInstanceId = queueRequestDetails.getInitiator();
    }

    protected QueueRequestSearchable() {
        //Required by Jackson
    }
}