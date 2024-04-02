package com.neo.util.framework.websocket.api;

import com.neo.util.framework.api.request.AbstractUserRequestDetails;
import com.neo.util.framework.api.request.RequestContext;

public class WebsocketRequestDetails extends AbstractUserRequestDetails {

    /**
     * The remote address of the caller
     */
    protected final String remoteAddress;

    public WebsocketRequestDetails(String traceId, String instanceId, String remoteAddress, RequestContext requestContext) {
        super(traceId, instanceId, requestContext);
        this.remoteAddress = remoteAddress;
    }

    /**
     * Create a new instance in order to get a new {@link #getRequestId()} for each request.
     *
     * @return a new instance
     */
    public WebsocketRequestDetails newInstance() {
        WebsocketRequestDetails requestDetails = new WebsocketRequestDetails(this.getTraceId(), this.getInstanceId(), this.getRemoteAddress(), this.getRequestContext());
        getUser().ifPresent(requestDetails::setUserIfPossible);
        return requestDetails;
    }

    public String getRemoteAddress() {
        return remoteAddress;
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
