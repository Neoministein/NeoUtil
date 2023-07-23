package com.neo.util.framework.rest.impl.request;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.percistence.request.RequestSearchable;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.percistence.HttpRequestSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HttpRequestRecorder extends AbstractRequestRecorder<HttpRequestDetails> {

    @Override
    public RequestSearchable parseToSearchable(HttpRequestDetails requestDetails, boolean failed) {
        return new HttpRequestSearchable(requestDetails, -1, "", "");
    }

    @Override
    public Class<HttpRequestDetails> getRequestType() {
        return HttpRequestDetails.class;
    }
}
