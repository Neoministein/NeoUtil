package com.neo.util.helidon.rest.security;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.rest.api.security.Secured;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.util.Set;

@Secured
@Provider
@ApplicationScoped
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    protected ObjectNode unauthorized;

    @Inject
    protected RequestDetails requestDetails;

    @Context
    protected ResourceInfo resourceInfo;

    @Inject
    protected ResponseGenerator responseGenerator;

    @PostConstruct
    public void init() {
        unauthorized = responseGenerator.errorObject("auth/001", "Forbidden");
    }

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        RolesAllowed rolesAllowed = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }
        if (!requestDetails.isInRoles(Set.of(rolesAllowed.value()))) {
            containerRequest.abortWith(responseGenerator.error(403,unauthorized));
        }
    }
}