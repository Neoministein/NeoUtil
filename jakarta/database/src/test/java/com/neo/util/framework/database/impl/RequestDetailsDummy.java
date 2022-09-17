package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.RandomString;
import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.security.RolePrincipal;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class RequestDetailsDummy implements RequestDetails {

    protected String requestId = new RandomString(32).nextString();
    protected Date receiveDate = new Date();
    protected RolePrincipal rolePrincipal = null;
    protected RequestContext requestContext = null;

    @Override
    public String getRemoteAddress() {
        return "127.0.0.1";
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public Optional<RolePrincipal> getUser() {
        return Optional.ofNullable(rolePrincipal);
    }

    @Override
    public void setUser(RolePrincipal principal) {
        this.rolePrincipal = principal;
    }

    @Override
    public boolean isInRole(String role) {
        return rolePrincipal.getRoles().contains(role);
    }

    @Override
    public boolean isInRoles(Collection<String> roles) {
        return rolePrincipal.getRoles().containsAll(roles);
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

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
