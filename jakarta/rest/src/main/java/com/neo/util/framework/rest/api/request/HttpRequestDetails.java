package com.neo.util.framework.rest.api.request;

import com.neo.util.framework.api.request.AbstractRequestDetails;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.security.RolePrincipal;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

/**
 * This impl consolidates all the data for a single http request.
 */
public class HttpRequestDetails extends AbstractRequestDetails {

    /**
     * Rhe remote address of the caller
     */
    protected final String remoteAddress;

    /**
     * The current user
     */
    protected RolePrincipal user;

    public HttpRequestDetails(String remoteAddress, String requestId, RequestContext requestContext) {
        super(requestId, requestContext);
        this.remoteAddress = remoteAddress;
    }


    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Optional<RolePrincipal> getUser() {
        return Optional.ofNullable(user);
    }

    /**
     * Set the current user
     */
    public void setUser(RolePrincipal user) {
        this.user = user;
    }

    /**
     * Checks if the current user has the role
     *
     * @param role the role to check for
     * @return true if the current user has the role
     */
    public boolean isInRole(String role) {
        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().contains(role)).orElse(false);
    }

    /**
     * Checks if the current user has all the roles
     *
     * @param roles the roles to check for
     * @return true if the current user has all the roles
     */
    public boolean hasAllRoles(Collection<String> roles) {
        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().containsAll(roles)).orElse(false);
    }

    /**
     * Checks if the current user has one the roles
     *
     * @param roles the roles to check for
     * @return true if the current user has all the roles
     */
    public boolean hasOneOfTheRoles(Collection<String> roles) {
        return getUser().map(rolePrincipal -> rolePrincipal.getRoles().stream().anyMatch(roles::contains)).orElse(false);
    }

    @Override
    public String getCaller() {
        return getUser().map(Principal::getName).orElse(getRequestId());
    }

    @Override
    public String toString() {
        return super.toString() + ", RemoteAddress=[" + remoteAddress + "]";
    }
}
