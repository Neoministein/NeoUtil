package com.neo.util.helidon.rest.connection;

import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.security.RolePrincipal;
import io.helidon.security.SecurityContext;
import io.helidon.webserver.ServerRequest;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Context;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@RequestScoped
public class RequestDetailsImpl implements RequestDetails {

    protected RolePrincipal rolePrincipal = null;
    protected Date receiveDate = new Date();

    protected RequestContext requestContext;

    @Context
    protected ServerRequest serverRequest;

    @Context
    protected SecurityContext securityContext;

    @Override
    public String getRemoteAddress() {
        return serverRequest.remoteAddress();
    }

    @Override
    public String getRequestId() {
        return securityContext.id();
    }

    @Override
    public Optional<RolePrincipal> getUser() {
        return Optional.ofNullable(rolePrincipal);
    }

    @Override
    public void setUser(RolePrincipal rolePrincipal) {
        this.rolePrincipal = rolePrincipal;
    }

    @Override
    public boolean isInRole(String role) {
        if (rolePrincipal == null) {
            return false;
        }
        return rolePrincipal.getRoles().contains(role);
    }

    @Override
    public boolean isInRoles(Collection<String> list) {
        if (rolePrincipal == null) {
            return false;
        }
        return rolePrincipal.getRoles().containsAll(list);
    }

    @Override
    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public Date getRequestReceiveDate() {
        return receiveDate;
    }

}
