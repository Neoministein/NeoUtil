package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.jakarta.request.recording.searchable.HttpRequestLogSearchable;
import com.neo.util.jakarta.request.recording.searchable.RequestLogSearchable;
import com.neo.util.jakarta.rest.HttpRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HttpRequestSearchableParser extends AbstractRequestSearchableParser<HttpRequestDetails> {

    @Override
    public RequestLogSearchable parse(HttpRequestDetails requestDetails, boolean failed) {
        return new HttpRequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<HttpRequestDetails> getRequestType() {
        return HttpRequestDetails.class;
    }
}
