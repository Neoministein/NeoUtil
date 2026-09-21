package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.api.event.StartupRequestDetails;
import com.neo.util.jakarta.request.recording.searchable.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StartupRequestSearchableParser extends AbstractRequestSearchableParser<StartupRequestDetails> {

    @Override
    public RequestLogSearchable parse(StartupRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<StartupRequestDetails> getRequestType() {
        return StartupRequestDetails.class;
    }
}
