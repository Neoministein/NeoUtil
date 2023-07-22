package com.neo.util.framework.rest.api.request;

import com.neo.util.framework.api.request.AbstractUserRequestDetails;
import com.neo.util.framework.api.request.RequestContext;

/**
 * This impl consolidates all the data for a single http request.
 */
public class HttpRequestDetails extends AbstractUserRequestDetails {

    /**
     * Rhe remote address of the caller
     */
    protected final String remoteAddress;

    public HttpRequestDetails(String remoteAddress, String requestId, RequestContext requestContext) {
        super(requestId, requestContext);
        this.remoteAddress = remoteAddress;
    }


    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String toString() {
        return super.toString() + ", RemoteAddress=[" + remoteAddress + "]";
    }

    public record Context(String httpMethod, String uri) implements RequestContext {
        @Override
        public String type() {
            return "Http";
        }

        @Override
        public String toString() {
            return httpMethod + " " + uri;
        }
    }
}
