package com.neo.util.framework.websocket.impl.interceptor;

import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.websocket.api.SecuredWebsocket;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import com.neo.util.framework.websocket.api.scope.WebsocketScope;
import com.neo.util.framework.websocket.api.scope.internal.NeoUtilWebsocketOnOpen;
import com.neo.util.framework.websocket.impl.WebsocketStateHolder;
import com.neo.util.framework.websocket.impl.scope.ScopeContext;
import com.networknt.org.apache.commons.validator.routines.InetAddressValidator;
import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Set;
import java.util.UUID;

@NeoUtilWebsocketOnOpen
@Interceptor
@Priority(PriorityConstants.PLATFORM_BEFORE)
public class WebsocketOnOpenInterceptor {

    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    protected static final String INVALID_IP = "255.255.255.255";

    protected final BeanManager beanManager;
    protected final RequestContextExecutor executor;
    protected final WebsocketStateHolder sessionHolder;

    protected final InstanceIdentification instanceIdentification;
    protected final HttpCredentialsGenerator credentialsGenerator;
    protected final AuthenticationProvider authenticationProvider;

    @Inject
    public WebsocketOnOpenInterceptor(BeanManager beanManager, RequestContextExecutor executor, WebsocketStateHolder sessionHolder,
                                      InstanceIdentification instanceIdentification, HttpCredentialsGenerator credentialsGenerator,
                                      AuthenticationProvider authenticationProvider) {
        this.beanManager = beanManager;
        this.executor = executor;
        this.sessionHolder = sessionHolder;
        this.instanceIdentification = instanceIdentification;
        this.credentialsGenerator = credentialsGenerator;
        this.authenticationProvider = authenticationProvider;
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        Session session = null;
        EndpointConfig config = null;

        for (Object o: invocationContext.getParameters()) {
            if (o instanceof Session x) {
                session = x;
            } else if (o instanceof EndpointConfig x) {
                config = x;
            }
        }

        if (session == null || config == null) {
            throw new IllegalStateException();
        }

        logic(invocationContext, session, config);
        return null;//
    }

    protected void logic(InvocationContext invocationContext, Session session, EndpointConfig config) throws Exception {
        ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(WebsocketScope.class);
        context.enter(session.getId());

        try {
            MultivaluedMap<String, String> headers = getStoredObject(config, HttpHeaders.class.getSimpleName());
            WebsocketRequestDetails requestDetails = createUserRequestDetails(session, headers);

            sessionHolder.setState(session, requestDetails);

            executor.executeChecked(requestDetails, () -> {
                boolean shouldDisconnect = authenticate(requestDetails, headers, getRequiredRoles(invocationContext));

                if (shouldDisconnect && isSecured(invocationContext)) {
                    session.close();
                } else {
                    invocationContext.proceed();
                }
            });
        } finally {
            context.exit(session.getId());
        }
    }

    public WebsocketRequestDetails createUserRequestDetails(Session session, MultivaluedMap<String, String> headers) {
        return new WebsocketRequestDetails(
                getTraceId(),
                instanceIdentification.getInstanceId(),
                getRemoteAddress(headers),
                new WebsocketRequestDetails.Context(session.getRequestURI().toString()));
    }

    public boolean authenticate(UserRequestDetails requestDetails, MultivaluedMap<String, String> headers, Set<String> roles) {
        boolean failed = true;
        try {
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                return failed;
            }

            Credential credential = credentialsGenerator.generate(authHeader);
            authenticationProvider.authenticate(requestDetails, credential);

            failed = !requestDetails.hasOneOfTheRoles(roles);
        } catch (ExternalRuntimeException ignored) {}

        return failed;
    }

    protected String getTraceId() {
        return UUID.randomUUID().toString();
    }

    protected boolean isSecured(InvocationContext invocationContext) {
        return invocationContext.getMethod().getClass().getAnnotation(SecuredWebsocket.class) != null;
    }

    protected Set<String> getRequiredRoles(InvocationContext invocationContext) {
        RolesAllowed rolesAllowed = invocationContext.getMethod().getClass().getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return Set.of();
        } else {
            return Set.of(rolesAllowed.value());
        }
    }

    /**
     * When pacing through proxies the remote address will no longer represent the original IP.
     * </p>
     * Therefor the X-Real-IP and X-Forwarded-For are checked before the socket address is returned.
     */
    protected String getRemoteAddress(MultivaluedMap<String, String> headers) {
        String remoteAddress = headers.getFirst(X_REAL_IP);

        if (remoteAddress != null && InetAddressValidator.getInstance().isValid(remoteAddress)) {
            return remoteAddress;
        }
        remoteAddress = headers.getFirst(X_FORWARDED_FOR);
        if (remoteAddress != null && InetAddressValidator.getInstance().isValid(remoteAddress)) {
            return remoteAddress;
        }
        return INVALID_IP;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getStoredObject(EndpointConfig config, String key) {
        return (T) config.getUserProperties().get(key);
    }
}
