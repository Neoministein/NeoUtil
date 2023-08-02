package com.neo.util.framework.websocket.api;

import com.neo.util.framework.api.request.AbstractUserRequestDetails;
import com.neo.util.framework.api.request.RequestContext;

public class WebsocketRequestDetails extends AbstractUserRequestDetails {

    public WebsocketRequestDetails(String traceId, String instanceId, RequestContext requestContext) {
        super(traceId, instanceId, requestContext);
    }

    @Override
    public void updateMDC() {
        super.updateMDC();
    }

    public record Context(String uri) implements RequestContext {
        @Override
        public String type() {
            return "Websocket";
        }

        @Override
        public String toString() {
            return uri;
        }
    }
}
