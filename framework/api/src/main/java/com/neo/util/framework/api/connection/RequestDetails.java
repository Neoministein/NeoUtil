package com.neo.util.framework.api.connection;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * Returns the current user's as an Optional UUID
     *
     * @return the current uuid
     */
    Optional<UUID> getUUId();

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
    boolean isInRoles(List<String> roles);

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
     * Sets the date and time when the system received the request
     *
     * @param startDate
     */
    void setRequestReceiveDate(Date startDate);

    /**
     * Returns the date the request has been received
     *
     * @return the date
     */
    Date getRequestReceiveDate();
}
