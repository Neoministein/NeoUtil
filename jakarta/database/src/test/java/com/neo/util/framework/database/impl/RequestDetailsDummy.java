package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.RandomString;
import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.connection.RequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class RequestDetailsDummy implements RequestDetails {

    protected String caller = "127.0.0.1";
    protected String requestId = new RandomString(32).nextString();
    protected Instant receiveDate = Instant.now();
    protected RequestContext requestContext = null;

    @Override
    public String getCaller() {
        return caller;
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
        return receiveDate;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }
}
