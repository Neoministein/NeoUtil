package com.neo.util.helidon.rest.security;

import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.impl.connection.HttpRequestDetails;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
import com.networknt.schema.format.InetAddressValidator;
import io.helidon.webserver.ServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.util.UUID;

@Provider
@Priority(100)
@ApplicationScoped
public class IdentificationFilter implements ContainerRequestFilter {

    public static final String X_REAL_IP = "X-Real-IP";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    protected final String instanceUuid = UUID.randomUUID().toString();

    @Inject
    protected RequestDetailsProducer requestDetailsProvider;

    @Context
    protected ServerRequest serverRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestDetailsProvider.setRequestDetails(new HttpRequestDetails(
                getRemoteAddress(serverRequest, requestContext.getHeaders()), instanceUuid + ":" + serverRequest.requestId(),
                new RequestContext.Http(requestContext.getMethod(), getUri(requestContext.getUriInfo()))));
    }

    protected String getUri(UriInfo uriInfo) {
        return uriInfo.getRequestUri().toString().substring(uriInfo.getBaseUri().toString().length());
    }

    /**
     * When pacing through proxies the remote address will no longer represent the original IP.
     * </p>
     * Therefor the X-Real-IP and X-Forwarded-For are checked before the socket address is returned.
     */
    protected String getRemoteAddress(ServerRequest serverRequest, MultivaluedMap<String, String> headers) {
        String remoteAddress = headers.getFirst(X_REAL_IP);

        if (remoteAddress != null && InetAddressValidator.getInstance().isValid(remoteAddress)) {
            return remoteAddress;
        }
        remoteAddress = headers.getFirst(X_FORWARDED_FOR);
        if (remoteAddress != null && InetAddressValidator.getInstance().isValid(remoteAddress)) {
            return remoteAddress;
        }
        return serverRequest.remoteAddress();
    }
}