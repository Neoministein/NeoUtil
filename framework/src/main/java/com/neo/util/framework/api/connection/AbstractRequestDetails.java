package com.neo.util.framework.api.connection;

import org.slf4j.MDC;

import java.util.Date;

public abstract class AbstractRequestDetails implements RequestDetails {

    protected final Date requestStartDate;
    protected final String requestId;
    protected final RequestContext requestContext;

    protected AbstractRequestDetails(String requestId, RequestContext requestContext) {
        MDC.put("traceId", requestId);
        this.requestStartDate = new Date();
        this.requestId = requestId;
        this.requestContext = requestContext;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public Date getRequestStartDate() {
        return requestStartDate;
    }

}
