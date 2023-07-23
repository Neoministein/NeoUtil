package com.neo.util.framework.impl.request.recording;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;
import com.neo.util.framework.percistence.request.RequestSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SchedulerRequestRecorder extends AbstractRequestRecorder<SchedulerRequestDetails> {

    @Override
    public RequestSearchable parseToSearchable(SchedulerRequestDetails requestDetails, boolean failed) {
        return new RequestSearchable(requestDetails, failed);
    }

    @Override
    public Class<SchedulerRequestDetails> getRequestType() {
        return SchedulerRequestDetails.class;
    }
}
