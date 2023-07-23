package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.percistence.request.RequestSearchable;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WebsocketRequestRecorder extends AbstractRequestRecorder<WebsocketRequestDetails> {

    @Override
    public RequestSearchable parseToSearchable(WebsocketRequestDetails requestDetails, boolean failed) {
        return new RequestSearchable(requestDetails, failed);
    }

    @Override
    public Class<WebsocketRequestDetails> getRequestType() {
        return WebsocketRequestDetails.class;
    }
}
