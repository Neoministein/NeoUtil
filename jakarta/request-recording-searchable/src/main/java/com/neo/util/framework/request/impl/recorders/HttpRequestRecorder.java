package com.neo.util.framework.request.impl.recorders;

import com.neo.util.framework.request.percistence.HttpRequestLogSearchable;
import com.neo.util.framework.request.percistence.RequestLogSearchable;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HttpRequestRecorder extends AbstractRequestRecorder<HttpRequestDetails> {

    @Override
    public RequestLogSearchable parse(HttpRequestDetails requestDetails, boolean failed) {
        return new HttpRequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<HttpRequestDetails> getRequestType() {
        return HttpRequestDetails.class;
    }
}
