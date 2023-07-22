package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.rest.api.security.Secured;

import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Secured
@Provider
@RequestScoped
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Context
    protected ResourceInfo resourceInfo;

    @Inject
    protected ResponseGenerator responseGenerator;

    @Inject
    protected UserRequestDetails userRequestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        LOGGER.trace("Accessing secured endpoint");
        RolesAllowed rolesAllowed = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }

        Set<String> roles = Set.of(rolesAllowed.value());
        if (!userRequestDetails.hasOneOfTheRoles(roles)) {
            LOGGER.info("Aborting request with forbidden, one of the permissions is required {}", roles);
            containerRequest.abortWith(responseGenerator.error(403, FrameworkConstants.EX_FORBIDDEN));
        }
    }
}