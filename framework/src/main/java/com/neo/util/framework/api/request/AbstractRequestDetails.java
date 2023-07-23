package com.neo.util.framework.api.request;

import org.slf4j.MDC;

import java.time.Instant;

public abstract class AbstractRequestDetails implements RequestDetails {

    protected final long requestId;
    protected final String instanceId;
    protected final RequestContext requestContext;
    protected final Instant requestStartDate;

    protected AbstractRequestDetails(String instanceId, RequestContext requestContext) {
        this.requestId = REQUEST_ID.addAndGet(1);
        this.instanceId = instanceId;
        this.requestStartDate = Instant.now();
        this.requestContext = requestContext;

        updateMDC();
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public Instant getRequestStartDate() {
        return requestStartDate;
    }

    protected void updateMDC() {
        MDC.put(MDC_REQUEST_ID, getRequestId() + "");
        MDC.put(MDC_REQUEST_CONTEXT_TYPE, getRequestContext().type());
    }

    @Override
    public String toString() {
        return "RequestId: [" + getRequestId()
                + "], instanceId: [" + getInstanceId()
                + "], Initiator: [" + getInitiator()
                + "], RequestStartDate: [" + getRequestStartDate().toString()
                + "], RequestContext: [" + getRequestContext() + "]";
    }
}
