package com.neo.util.helidon.rest.security;

import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestDetailsProducer;
import com.neo.util.framework.rest.impl.security.IdentificationFilter;
import io.helidon.webserver.http.ServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Specializes
@ApplicationScoped
public class HelidonRequestIdentificationFilter extends IdentificationFilter {

    @Context
    protected ServerRequest serverRequest;

    @Inject
    public HelidonRequestIdentificationFilter(InstanceIdentification identification, RequestDetailsProducer requestDetailsProvider) {
        super(identification, requestDetailsProvider);
    }

    @Override
    protected String getRemoteAddress(ContainerRequestContext requestContext) {
        String url = super.getRemoteAddress(requestContext);

        if (IdentificationFilter.INVALID_IP.equals(url)) {
            return serverRequest.remotePeer().host();
        }
        return url;
    }
}