package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestDetailsProducer;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.networknt.org.apache.commons.validator.routines.InetAddressValidator;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.UUID;

@Provider
@Priority(100)
@ApplicationScoped
public class IdentificationFilter implements ContainerRequestFilter {

    public static final String X_REAL_IP = "X-Real-IP";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    protected static final String INVALID_IP = "255.255.255.255";

    @Inject
    protected InstanceIdentification identification;

    @Inject
    protected RequestDetailsProducer requestDetailsProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestDetailsProvider.setRequestDetails(
                new HttpRequestDetails(
                        getTraceId(),
                        identification.getInstanceId(),
                        getRemoteAddress(requestContext),
                        getUserAgent(requestContext),
                        parseRequestContext(requestContext)));
    }

    protected String getTraceId() {
        return UUID.randomUUID().toString();
    }

    protected RequestContext parseRequestContext(ContainerRequestContext requestContext) {
        return new HttpRequestDetails.Context(requestContext.getMethod(), parseURI(requestContext.getUriInfo()));
    }

    /**
     * When pacing through proxies the remote address will no longer represent the original IP.
     * </p>
     * Therefor the X-Real-IP and X-Forwarded-For are checked before the socket address is returned.
     */
    protected String getRemoteAddress(ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> headers = requestContext.getHeaders();

        String remoteAddress = headers.getFirst(X_REAL_IP);

        if (remoteAddress != null && InetAddressValidator.getInstance().isValid(remoteAddress)) {
            return remoteAddress;
        }
        remoteAddress = headers.getFirst(X_FORWARDED_FOR);
        if (remoteAddress != null && InetAddressValidator.getInstance().isValid(remoteAddress)) {
            return remoteAddress;
        }
        return INVALID_IP;
    }

    protected String getUserAgent(ContainerRequestContext requestContext) {
        List<String> useragent = requestContext.getHeaders().get(HttpHeaders.USER_AGENT);
        if (useragent == null) {
            return null;
        } else {
            return useragent.toString();
        }
    }

    protected String parseURI(UriInfo uriInfo) {
        return uriInfo.getRequestUri().toString().substring(uriInfo.getBaseUri().toString().length());
    }
}