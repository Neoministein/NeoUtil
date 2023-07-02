package com.neo.util.framework.api.request;

import com.neo.util.common.impl.json.JsonUtil;
import org.slf4j.MDC;

import java.time.Instant;

public abstract class AbstractRequestDetails implements RequestDetails {

    protected final Instant requestStartDate;
    protected final String requestId;
    protected final RequestContext requestContext;

    protected AbstractRequestDetails(String requestId, RequestContext requestContext) {
        MDC.put("traceId", requestId);
        this.requestStartDate = Instant.now();
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
    public Instant getRequestStartDate() {
        return requestStartDate;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
