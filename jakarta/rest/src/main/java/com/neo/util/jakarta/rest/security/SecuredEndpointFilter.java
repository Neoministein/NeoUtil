package com.neo.util.jakarta.rest.security;

import com.neo.util.api.request.UserRequestDetails;
import com.neo.util.api.security.AuthenticationProvider;
import com.neo.util.api.security.SecurityConstants;
import com.neo.util.jakarta.request.UserRequest;
import com.neo.util.jakarta.rest.response.ClientResponseService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecuredResource
@Provider
@RequestScoped
@Priority(Priorities.AUTHENTICATION + 1)
public class SecuredEndpointFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuredEndpointFilter.class);

    @Inject
    protected ClientResponseService clientResponseService;

    @Inject
    protected AuthenticationProvider authenticationProvider;

    @Inject
    @UserRequest
    protected UserRequestDetails userRequestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        if (!authenticationProvider.isSecurityEnabled()) {
            LOGGER.warn("Security is disabled, this should only be active in development");
            return;
        }
        if (userRequestDetails.getUser().isEmpty()) {
            LOGGER.info("Aborting request with unauthorized");
            containerRequest.abortWith(clientResponseService.error(401, SecurityConstants.EX_UNAUTHORIZED));
        }
    }
}
