package com.neo.util.helidon.rest.security;

import com.neo.util.framework.rest.impl.security.IdentificationFilter;
import io.helidon.webserver.ServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Specializes
@ApplicationScoped
public class HelidonRequestIdentificationFilter extends IdentificationFilter {

    @Context
    protected ServerRequest serverRequest;

    @Override
    protected String getRemoteAddress(ContainerRequestContext requestContext) {
        String url = super.getRemoteAddress(requestContext);

        if (IdentificationFilter.INVALID_IP.equals(url)) {
            return serverRequest.remoteAddress();
        }
        return url;
    }
}