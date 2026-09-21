package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.api.scheduler.SchedulerRequestDetails;
import com.neo.util.jakarta.request.recording.searchable.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SchedulerRequestSearchableParser extends AbstractRequestSearchableParser<SchedulerRequestDetails> {

    @Override
    public RequestLogSearchable parse(SchedulerRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<SchedulerRequestDetails> getRequestType() {
        return SchedulerRequestDetails.class;
    }
}