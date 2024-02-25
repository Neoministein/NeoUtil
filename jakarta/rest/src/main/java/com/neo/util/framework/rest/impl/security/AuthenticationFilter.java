package com.neo.util.framework.rest.impl.security;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.framework.api.request.UserRequest;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter provides base authentication functionality based on what the {@link AuthenticationProvider} and {@link  HttpCredentialsGenerator} supports
 */
@Provider
@RequestScoped
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Inject
    protected AuthenticationProvider authenticationProvider;

    @Inject
    protected HttpCredentialsGenerator httpCredentialsGenerator;

    @Inject
    @UserRequest
    protected UserRequestDetails requestDetails;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        String authorizationHeader = containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);

        if(StringUtils.isEmpty(authorizationHeader)) {
            return;
        }

        LOGGER.trace("Authentication attempt");
        try {
            Credential credential = httpCredentialsGenerator.generate(authorizationHeader);
            authenticationProvider.authenticate(requestDetails, credential);
        } catch (CommonRuntimeException ex) {
            LOGGER.debug("Invalid authorization header [{}]", ex.getExceptionId());
        }
    }
}