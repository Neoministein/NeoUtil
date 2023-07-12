package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.rest.api.security.Secured;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Secured
@Provider
@RequestScoped
@Priority(Priorities.AUTHENTICATION + 1)
public class SecuredEndpointFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuredEndpointFilter.class);

    @Inject
    protected ResponseGenerator responseGenerator;

    @Inject
    protected RequestDetails requestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        if (((HttpRequestDetails) requestDetails).getUser().isEmpty()) {
            LOGGER.info("Aborting request with unauthorized");
            containerRequest.abortWith(responseGenerator.error(401, FrameworkConstants.EX_UNAUTHORIZED));
        }
    }
}
