package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.connection.RequestDetails;
import org.slf4j.MDC;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(100)
public class IdentificationFilter implements ContainerRequestFilter {

    @Inject
    protected RequestDetails requestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        requestDetails.setRequestContext(new RequestContext.Http(containerRequest.getMethod(), getUri(containerRequest.getUriInfo())));
        MDC.put("traceId", requestDetails.getRequestId());
    }

    protected String getUri(UriInfo uriInfo) {
        return uriInfo.getRequestUri().toString().substring(uriInfo.getBaseUri().toString().length());
    }
}