package com.neo.util.framework.websocket.impl.security;

import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.websocket.impl.interceptor.WebsocketInterceptorLogicImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;

import java.util.Set;

@ApplicationScoped
@Specializes
public class CustomWebsocketInterceptorLogicImpl extends WebsocketInterceptorLogicImpl {

    private Set<String> requiredRoles = Set.of();

    @Inject
    public CustomWebsocketInterceptorLogicImpl(RequestContextExecutor executor, InstanceIdentification instanceIdentification, HttpCredentialsGenerator credentialsGenerator, AuthenticationProvider authenticationProvider) {
        super(executor, instanceIdentification, credentialsGenerator, authenticationProvider);
    }


    @Override
    protected Set<String> getPermittedRoles(InvocationContext invocationContext) {
        return requiredRoles;
    }

    public void setRequiredRoles(Set<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }
}
