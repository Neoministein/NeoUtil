package com.neo.util.framework.rest.percistence;

import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.percistence.request.RequestSearchable;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;

@SearchableIndex(indexName = RequestSearchable.INDEX_NAME, indexPeriod = IndexPeriod.DAILY)
public class HttpRequestSearchable extends RequestSearchable implements Searchable {

    protected String remoteAddress;
    protected String status;
    protected String agent;
    protected String error;

    public HttpRequestSearchable(HttpRequestDetails httpRequestDetails, int status, String agent, String error) {
        super(httpRequestDetails, status >= 400);
        this.remoteAddress = httpRequestDetails.getRemoteAddress();
        this.status = Integer.toString(status);
        this.agent = agent;
        this.error = error;
        this.initiator = httpRequestDetails.getUser().map(RolePrincipal::getName).orElse(null);
    }

    protected HttpRequestSearchable() {
        //Required by Jackson
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getStatus() {
        return status;
    }

    public String getAgent() {
        return agent;
    }

    public String getError() {
        return error;
    }
}