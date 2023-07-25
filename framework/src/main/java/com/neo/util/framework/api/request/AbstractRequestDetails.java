package com.neo.util.framework.api.request;

import org.slf4j.MDC;

import java.time.Instant;

public abstract class AbstractRequestDetails implements RequestDetails {

    protected final String traceId;
    protected final long requestId;
    protected final String instanceId;
    protected final RequestContext requestContext;
    protected final Instant requestStartDate;

    protected AbstractRequestDetails(String traceId, String instanceId, RequestContext requestContext) {
        this.traceId = traceId;
        this.instanceId = instanceId;
        this.requestStartDate = Instant.now();
        this.requestContext = requestContext;
        this.requestId = REQUEST_ID.addAndGet(1);

        updateMDC();
    }

    @Override
    public String getTraceId() {
        return traceId;
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
        MDC.put(MDC_TRACE_ID, getTraceId());
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
