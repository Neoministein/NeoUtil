package com.neo.util.helidon.rest.connection;

import com.neo.util.framework.api.connection.HttpRequestDetails;
import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.security.RolePrincipal;

import java.util.Date;
import java.util.Optional;

public class HelidonHttpRequestDetails implements HttpRequestDetails {

    protected  Date requestStartDate;
    protected  String remoteAddress;
    protected  String requestId;
    protected  RequestContext requestContext;
    protected RolePrincipal rolePrincipal = null;



    public HelidonHttpRequestDetails(String remoteAddress, String requestId, RequestContext requestContext) {
        this.requestStartDate = new Date();
        this.remoteAddress = remoteAddress;
        this.requestId = requestId;
        this.requestContext = requestContext;
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Optional<RolePrincipal> getUser() {
        return Optional.ofNullable(rolePrincipal);
    }

    @Override
    public void setUser(RolePrincipal user) {
        this.rolePrincipal = user;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Override public Date getRequestStartDate() {
        return requestStartDate;
    }

}
