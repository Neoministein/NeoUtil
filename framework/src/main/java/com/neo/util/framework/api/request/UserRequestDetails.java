package com.neo.util.framework.api.request;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.security.RolePrincipal;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

/**
 * This interface represents a request which can have a user attached to it.
 * It should only be used by classes which are explicitly called by a user.
 * <p>
 * Such as:
 * <p>
 *  - Jax-Rs Resource or Filter
 * <p>
 *  - Websocket Endpoint
 */
public interface UserRequestDetails extends RequestDetails {

    ExceptionDetails EX_USER_ALREADY_DEFINED = new ExceptionDetails(
            "auth/user-already-defined", "A user is already defined in this context", true);

    /**
     * Returns the current user associated with the request
     */
    Optional<RolePrincipal> getUser();

    /**
     * Sets the current user
     * @throws ValidationException {@link UserRequestDetails#EX_USER_ALREADY_DEFINED} if the current user is already set.
     */
    void setUserIfPossible(RolePrincipal user) throws ValidationException;

    /**
     * Returns the initiator of the request
     */
    default String getInitiator() {
        return getUser().map(Principal::getName).orElse(getFullRequestId());
    }

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
     * <p>
     * Will return false when there is no user present and collection is empty.
     *
     * @param roles the roles to check for
     * @return true if the current user has all the roles
     */
    default boolean hasAllRoles(Collection<String> roles) {
        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().containsAll(roles)).orElse(false);
    }

    /**
     * Checks if the current user has one the roles
     * <p>
     * Will return false when there is no user present and collection is empty.
     *
     * @param roles the roles to check for
     * @return true if the current user has one the roles
     */
    default boolean hasOneOfTheRoles(Collection<String> roles) {
        if (getUser().isPresent() && roles.isEmpty()) {
            return true;
        }

        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().stream().anyMatch(roles::contains)).orElse(false);
    }
}
