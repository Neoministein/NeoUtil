package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.request.RequestDetails;

import java.time.Instant;
import java.util.UUID;

public class DummyRequestDetails implements RequestDetails {

    protected long requestId = REQUEST_ID.addAndGet(1);
    protected String caller = UUID.randomUUID().toString();
    protected String instanceId = UUID.randomUUID().toString();
    protected String traceId = UUID.randomUUID().toString();
    protected Instant receiveDate = Instant.now();
    protected RequestContext requestContext = null;

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public String getInitiator() {
        return "Dummy:" + caller;
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
        return receiveDate;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }
}
