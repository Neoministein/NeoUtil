package com.neo.util.framework.rest.api.request;

import com.neo.util.framework.api.request.AbstractUserRequestDetails;
import com.neo.util.framework.api.request.RequestContext;

/**
 * This impl consolidates all the data for a single http request.
 */
public class HttpRequestDetails extends AbstractUserRequestDetails {

    /**
     * The remote address of the caller
     */
    protected final String remoteAddress;

    protected final String agent;

    protected int status = -1;

    protected String error = null;

    public HttpRequestDetails(String traceId, String instanceId, String remoteAddress, String  agent, RequestContext requestContext) {
        super(traceId, instanceId ,requestContext);
        this.remoteAddress = remoteAddress;
        this.agent = agent;
    }


    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getAgent() {
        return agent;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
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
