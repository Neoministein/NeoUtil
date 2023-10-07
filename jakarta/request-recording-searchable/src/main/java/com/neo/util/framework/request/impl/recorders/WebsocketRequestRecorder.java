package com.neo.util.framework.request.impl.recorders;

import com.neo.util.framework.request.percistence.RequestLogSearchable;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WebsocketRequestRecorder extends AbstractRequestRecorder<WebsocketRequestDetails> {

    @Override
    public RequestLogSearchable parse(WebsocketRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<WebsocketRequestDetails> getRequestType() {
        return WebsocketRequestDetails.class;
    }
}
