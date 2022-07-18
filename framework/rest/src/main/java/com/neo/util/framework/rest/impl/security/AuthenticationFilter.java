package com.neo.util.framework.rest.impl.security;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.rest.api.security.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.Credential;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.util.Optional;

/**
 * This filter provides base authentication functionality based on what the {@link AuthenticationProvider} and {@link  CredentialsGenerator} supports
 */
@Secured
@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    protected ObjectNode unauthorized;

    @Inject
    protected AuthenticationProvider authenticationProvider;

    @Inject
    protected ResponseGenerator responseGenerator;

    @Inject
    protected CredentialsGenerator credentialsGenerator;

    @Inject
    protected RequestDetails requestDetails;

    @PostConstruct
    protected void init() {
        unauthorized = responseGenerator.errorObject("auth/000", "Unauthorized");
    }

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        LOGGER.debug("Authentication attempt");
        String authorizationHeader = containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);

        try {
            Credential credential = credentialsGenerator.generate(authorizationHeader);
            Optional<RolePrincipal> principalOptional = authenticationProvider.authenticate(credential);
            if (principalOptional.isPresent()) {
                LOGGER.debug("Authentication success");
                requestDetails.setUser(principalOptional.get());
                return;
            }
            LOGGER.debug("Authentication failure");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            LOGGER.debug("Invalid authorization header");
        }
        abortWithUnauthorized(containerRequest);
    }

    protected void abortWithUnauthorized(ContainerRequestContext containerRequest) {
        LOGGER.info("Aborting request with unauthorized");
        containerRequest.abortWith(responseGenerator.error(401, unauthorized));
    }
}