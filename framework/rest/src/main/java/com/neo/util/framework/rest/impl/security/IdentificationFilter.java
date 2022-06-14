package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.connection.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.Date;

@Provider
@Priority(100)
public class IdentificationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentificationFilter.class);

    @Inject
    RequestDetails requestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        requestDetails.setRequestContext(new RequestContext(containerRequest.getMethod(), getUri(containerRequest.getUriInfo())));
        requestDetails.setRequestReceiveDate(new Date());
        MDC.put("traceId", requestDetails.getRequestId());
        LOGGER.debug("{} {}", requestDetails.getRemoteAddress(), requestDetails.getRequestContext());
    }

    protected String getUri(UriInfo uriInfo) {
        return uriInfo.getRequestUri().toString().substring(uriInfo.getBaseUri().toString().length());
    }
}