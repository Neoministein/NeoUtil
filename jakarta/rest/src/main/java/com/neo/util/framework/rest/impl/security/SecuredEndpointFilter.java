package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.request.UserRequest;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.rest.api.response.ClientResponseService;
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
    protected ClientResponseService clientResponseService;

    @Inject
    @UserRequest
    protected UserRequestDetails userRequestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        if (userRequestDetails.getUser().isEmpty()) {
            LOGGER.info("Aborting request with unauthorized");
            containerRequest.abortWith(clientResponseService.error(401, FrameworkConstants.EX_UNAUTHORIZED));
        }
    }
}
