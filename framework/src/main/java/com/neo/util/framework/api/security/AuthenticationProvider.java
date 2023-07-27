package com.neo.util.framework.api.security;

import com.neo.util.framework.api.request.UserRequestDetails;
import jakarta.security.enterprise.credential.Credential;

import java.util.List;
import java.util.Optional;

/**
 * This interface authenticates a user based on the provided credentials that type is supported.
 */
public interface AuthenticationProvider {

    /**
     * Authenticated and returns an {@link Optional<RolePrincipal>} based if it succeeds
     *
     * @param credential to authenticate against
     *
     * @return the role principal if authentication is successful
     */
    Optional<RolePrincipal> authenticate(Credential credential);

    /**
     * Authenticates the given {@link UserRequestDetails}
     *
     * @param userRequestDetails the user to authenticate
     * @param credential to authenticate against
     */
    void authenticate(UserRequestDetails userRequestDetails, Credential credential);

    /**
     * Returns which authentication scheme the provider supports
     *
     * @return a list of supported schemes
     */
    List<String> getSupportedAuthenticationSchemes();
}
