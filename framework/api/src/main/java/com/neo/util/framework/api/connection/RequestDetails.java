package com.neo.util.framework.api.connection;

import com.neo.util.framework.api.security.RolePrincipal;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * This interface consolidates all the data for a single http request.
 */
public interface RequestDetails {

    /**
     * Returns the address of the caller
     *
     * @return the address of the caller
     */
    String getRemoteAddress();

    /**
     * A unique identifier for the current http call
     *
     * @return unique identifier
     */
    String getRequestId();

    /**
     * Returns the current user's as an {@link Optional<RolePrincipal>}
     *
     * @return the current uuid
     */
    Optional<RolePrincipal> getUser();

    /**
     * Sets the principal of the current request
     *
     * @param principal
     */
    void setUser(RolePrincipal principal);

    /**
     * Checks if the current user has the role
     *
     * @param role the role to check for
     * @return true if the current user has the role
     */
    boolean isInRole(String role);

    /**
     * Checks if the current user has all the roles
     *
     * @param roles the roles to check for
     * @return true if the current user has all the roles
     */
    boolean isInRoles(Collection<String> roles);

    /**
     * The current context of the request
     *
     * @return the current context
     */
    RequestContext getRequestContext();

    /**
     * Sets the current requestContext
     *
     * @param requestContext the current requestContext
     */
    void setRequestContext(RequestContext requestContext);

    /**
     * Returns the date the request has been received
     *
     * @return the date
     */
    Date getRequestReceiveDate();
}
