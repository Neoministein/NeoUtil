package com.neo.util.framework.api.connection;

import com.neo.util.framework.api.security.RolePrincipal;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

/**
 * This interface consolidates all the data for a single http request.
 */
public interface HttpRequestDetails extends RequestDetails {

    /**
     * Returns the remote address
     */
    String getRemoteAddress();

    /**
     * Returns the current user's as an {@link Optional<RolePrincipal>}
     */
    Optional<RolePrincipal> getUser();

    /**
     * Set the current user
     */
    void setUser(RolePrincipal user);

    /**
     * Checks if the current user has the role
     *
     * @param role the role to check for
     * @return true if the current user has the role
     */
    default boolean isInRole(String role) {
        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().contains(role)).orElse(false);
    }

    /**
     * Checks if the current user has all the roles
     *
     * @param roles the roles to check for
     * @return true if the current user has all the roles
     */
    default boolean isInRoles(Collection<String> roles) {
        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().containsAll(roles)).orElse(false);
    }

    @Override
    default String getCaller() {
        return getUser().map(Principal::getName).orElse(getRequestId());
    }
}
