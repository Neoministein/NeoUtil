package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.impl.connection.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.rest.api.security.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
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

    @Inject
    protected AuthenticationProvider authenticationProvider;

    @Inject
    protected ResponseGenerator responseGenerator;

    @Inject
    protected CredentialsGenerator credentialsGenerator;

    @Inject
    protected RequestDetails requestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        LOGGER.trace("Authentication attempt");
        String authorizationHeader = containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);

        try {
            Credential credential = credentialsGenerator.generate(authorizationHeader);
            Optional<RolePrincipal> principalOptional = authenticationProvider.authenticate(credential);
            if (principalOptional.isPresent()) {
                LOGGER.trace("Authentication success");
                ((HttpRequestDetails) requestDetails).setUser(principalOptional.get());
                return;
            }
            LOGGER.info("Authentication failure");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            LOGGER.info("Invalid authorization header");
        }
        abortWithUnauthorized(containerRequest);
    }

    protected void abortWithUnauthorized(ContainerRequestContext containerRequest) {
        LOGGER.info("Aborting request with unauthorized");
        containerRequest.abortWith(responseGenerator.error(401, FrameworkConstants.EX_UNAUTHORIZED));
    }
}