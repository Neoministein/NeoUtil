package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.connection.AbstractRequestDetails;

import java.util.UUID;

public class StartupRequestDetails extends AbstractRequestDetails {

    protected StartupRequestDetails(String stage) {
        super(stage + ":" + UUID.randomUUID(), new StartupContext(stage));
    }

    @Override
    public String getCaller() {
        return requestContext.toString();
    }
}
