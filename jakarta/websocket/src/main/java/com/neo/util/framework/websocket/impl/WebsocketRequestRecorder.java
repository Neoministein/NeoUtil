package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.percistence.request.RequestLogSearchable;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WebsocketRequestRecorder extends AbstractRequestRecorder<WebsocketRequestDetails> {

    @Override
    public RequestLogSearchable parseToSearchable(WebsocketRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<WebsocketRequestDetails> getRequestType() {
        return WebsocketRequestDetails.class;
    }
}
