package com.neo.util.framework.websocket.impl.interceptor;

import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.websocket.api.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.WebsocketInterceptorLogic;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import com.neo.util.framework.websocket.api.WebsocketScope;
import com.neo.util.framework.websocket.impl.InterceptorWebsocketStateHolder;
import com.neo.util.framework.websocket.impl.scope.ScopeContext;
import com.networknt.org.apache.commons.validator.routines.InetAddressValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class WebsocketInterceptorLogicImpl implements WebsocketInterceptorLogic {

    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String INVALID_IP = "255.255.255.255";

    protected final BeanManager beanManager;
    protected final RequestContextExecutor executor;
    protected final InterceptorWebsocketStateHolder sessionHolder;

    protected final InstanceIdentification instanceIdentification;
    protected final HttpCredentialsGenerator credentialsGenerator;
    protected final AuthenticationProvider authenticationProvider;

    @Inject
    public WebsocketInterceptorLogicImpl(BeanManager beanManager, RequestContextExecutor executor, InterceptorWebsocketStateHolder sessionHolder,
                                      InstanceIdentification instanceIdentification, HttpCredentialsGenerator credentialsGenerator,
                                      AuthenticationProvider authenticationProvider) {
        this.beanManager = beanManager;
        this.executor = executor;
        this.sessionHolder = sessionHolder;
        this.instanceIdentification = instanceIdentification;
        this.credentialsGenerator = credentialsGenerator;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void onOpen(InvocationContext invocationContext, Session session, EndpointConfig config) throws Exception {
        ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(WebsocketScope.class);
        context.enter(session.getId());

        try {
            MultivaluedMap<String, String> headers = getStoredObject(config, HttpHeaders.class.getSimpleName());
            WebsocketRequestDetails requestDetails = createUserRequestDetails(session, headers);

            sessionHolder.setState(session, requestDetails, getMessageFromContext(invocationContext), isMonitored(invocationContext));

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

    @Override
    public void onMessage(InvocationContext invocationContext, Session session) throws Exception {
        ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(WebsocketScope.class);
        context.enter(session.getId());

        try {
            sessionHolder.addToIncomingSocketLog(invocationContext);
            executor.executeChecked(sessionHolder.getRequestDetails(), invocationContext::proceed);
        } finally {
            context.exit(session.getId());
        }
    }

    @Override
    public void onClose(InvocationContext invocationContext, Session session) throws Exception {
        ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(WebsocketScope.class);
        context.enter(session.getId());

        try {
            executor.executeChecked(sessionHolder.getRequestDetails(), invocationContext::proceed);
        } finally {
            context.exit(session.getId());
            context.destroy(session.getId());
        }
    }

    protected WebsocketRequestDetails createUserRequestDetails(Session session, MultivaluedMap<String, String> headers) {
        return new WebsocketRequestDetails(
                getTraceId(),
                instanceIdentification.getInstanceId(),
                getRemoteAddress(headers),
                new WebsocketRequestDetails.Context(session.getRequestURI().toString()));
    }

    protected boolean authenticate(UserRequestDetails requestDetails, MultivaluedMap<String, String> headers, Set<String> roles) {
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
        return invocationContext.getMethod().getDeclaringClass().getAnnotation(NeoUtilWebsocket.class).secured();
    }

    protected Set<String> getRequiredRoles(InvocationContext invocationContext) {
        return Set.of(invocationContext.getMethod().getDeclaringClass().getAnnotation(NeoUtilWebsocket.class).roles());
    }

    protected boolean isMonitored(InvocationContext invocationContext) {
        return invocationContext.getMethod().getDeclaringClass().getAnnotation(NeoUtilWebsocket.class).monitored();
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

    protected Function<InvocationContext, String> getMessageFromContext(InvocationContext invocationContext) {
        for (Method method: invocationContext.getMethod().getDeclaringClass().getMethods()) {
            if (method.getAnnotation(OnMessage.class) != null) {
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> clazz = parameters[i].getType();
                    if (clazz.equals(String.class) && parameters[i].getAnnotations().length == 0) {
                        final int index = i;
                        return invocationContext1 -> (String) invocationContext1.getParameters()[index];
                    }
                }
            }
        }

        return x -> "";
    }
}
