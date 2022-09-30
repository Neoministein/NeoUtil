package com.neo.util.helidon.rest.security;

import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
import com.neo.util.helidon.rest.connection.HelidonHttpRequestDetails;
import io.helidon.security.SecurityContext;
import io.helidon.webserver.ServerRequest;
import jakarta.ws.rs.core.Context;
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
    protected RequestDetailsProducer requestDetailsProvider;

    @Context
    protected ServerRequest serverRequest;

    @Context
    protected SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        requestDetailsProvider.setRequestDetails(new HelidonHttpRequestDetails(serverRequest.remoteAddress(), securityContext.id(),
                new RequestContext.Http(containerRequest.getMethod(), getUri(containerRequest.getUriInfo()))));
        MDC.put("traceId", securityContext.id());
    }

    protected String getUri(UriInfo uriInfo) {
        return uriInfo.getRequestUri().toString().substring(uriInfo.getBaseUri().toString().length());
    }
}