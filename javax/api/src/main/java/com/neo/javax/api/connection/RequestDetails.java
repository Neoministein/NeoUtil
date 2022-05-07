package com.neo.javax.api.connection;

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
}
