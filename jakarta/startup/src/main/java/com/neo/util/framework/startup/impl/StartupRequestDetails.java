package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.request.AbstractRequestDetails;

import java.util.UUID;

public class StartupRequestDetails extends AbstractRequestDetails {

    public StartupRequestDetails(String stage) {
        super(stage + ":" + UUID.randomUUID(), new StartupContext(stage));
    }

    @Override
    public String getCaller() {
        return requestContext.toString();
    }
}
