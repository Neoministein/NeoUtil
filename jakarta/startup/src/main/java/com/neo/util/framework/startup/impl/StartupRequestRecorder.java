package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.percistence.request.RequestSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StartupRequestRecorder extends AbstractRequestRecorder<StartupRequestDetails> {

    @Override
    public RequestSearchable parseToSearchable(StartupRequestDetails requestDetails, boolean failed) {
        return new RequestSearchable(requestDetails, failed);
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public Class<StartupRequestDetails> getRequestType() {
        return StartupRequestDetails.class;
    }
}
