package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.percistence.request.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StartupRequestRecorder extends AbstractRequestRecorder<StartupRequestDetails> {

    @Override
    public RequestLogSearchable parseToSearchable(StartupRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<StartupRequestDetails> getRequestType() {
        return StartupRequestDetails.class;
    }
}
