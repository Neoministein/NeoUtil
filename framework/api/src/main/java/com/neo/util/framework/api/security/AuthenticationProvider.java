package com.neo.util.framework.api.security;

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
     * @return
     */
    Optional<RolePrincipal> authenticate(Credential credential);

    /**
     * Returns which authentication scheme the provider supports
     *
     * @return a list of supported schemes
     */
    List<String> getSupportedAuthenticationSchemes();
}
