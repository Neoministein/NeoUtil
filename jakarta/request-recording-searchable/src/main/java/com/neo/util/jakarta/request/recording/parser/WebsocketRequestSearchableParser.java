package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.jakarta.request.recording.searchable.RequestLogSearchable;
import com.neo.util.jakarta.websocket.WebsocketRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WebsocketRequestSearchableParser extends AbstractRequestSearchableParser<WebsocketRequestDetails> {

    @Override
    public RequestLogSearchable parse(WebsocketRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<WebsocketRequestDetails> getRequestType() {
        return WebsocketRequestDetails.class;
    }
}
