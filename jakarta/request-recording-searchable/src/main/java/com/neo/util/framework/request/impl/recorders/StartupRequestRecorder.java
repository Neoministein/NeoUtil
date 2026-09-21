package com.neo.util.framework.request.impl.recorders;

import com.neo.util.api.event.StartupRequestDetails;
import com.neo.util.framework.request.percistence.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StartupRequestRecorder extends AbstractRequestRecorder<StartupRequestDetails> {

    @Override
    public RequestLogSearchable parse(StartupRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<StartupRequestDetails> getRequestType() {
        return StartupRequestDetails.class;
    }
}
