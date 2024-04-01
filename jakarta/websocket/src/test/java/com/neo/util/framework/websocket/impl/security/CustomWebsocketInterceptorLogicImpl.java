package com.neo.util.framework.websocket.impl.security;

import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.websocket.impl.InterceptorWebsocketStateHolder;
import com.neo.util.framework.websocket.impl.interceptor.WebsocketInterceptorLogicImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;

import java.util.Set;

@ApplicationScoped
@Specializes
public class CustomWebsocketInterceptorLogicImpl extends WebsocketInterceptorLogicImpl {

    private Set<String> requiredRoles = Set.of();

    @Inject
    public CustomWebsocketInterceptorLogicImpl(BeanManager beanManager, RequestContextExecutor executor, InterceptorWebsocketStateHolder sessionHolder, InstanceIdentification instanceIdentification, HttpCredentialsGenerator credentialsGenerator, AuthenticationProvider authenticationProvider) {
        super(beanManager, executor, sessionHolder, instanceIdentification, credentialsGenerator, authenticationProvider);
    }

    @Override
    protected Set<String> getRequiredRoles(InvocationContext invocationContext) {
        return requiredRoles;
    }

    public void setRequiredRoles(Set<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }
}
