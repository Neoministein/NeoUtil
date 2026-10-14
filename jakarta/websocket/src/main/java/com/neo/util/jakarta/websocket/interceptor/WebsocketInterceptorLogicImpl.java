package com.neo.util.jakarta.websocket.interceptor;

import com.neo.util.api.request.UserRequestDetails;
import com.neo.util.api.security.AuthenticationProvider;
import com.neo.util.api.security.HttpCredentialsGenerator;
import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.jakarta.request.InstanceIdentification;
import com.neo.util.jakarta.request.RequestContextExecutor;
import com.neo.util.jakarta.websocket.*;
import com.networknt.org.apache.commons.validator.routines.InetAddressValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebsocketInterceptorLogicImpl implements WebsocketInterceptorLogic {

    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String INVALID_IP = "255.255.255.255";


    protected final Map<String, WebsocketStateContext> websocketStateContextMap;

    protected final RequestContextExecutor executor;
    protected final InstanceIdentification instanceIdentification;
    protected final HttpCredentialsGenerator credentialsGenerator;
    protected final AuthenticationProvider authenticationProvider;

    @Inject
    public WebsocketInterceptorLogicImpl(RequestContextExecutor executor, InstanceIdentification instanceIdentification,
                                         HttpCredentialsGenerator credentialsGenerator, AuthenticationProvider authenticationProvider) {
        this.websocketStateContextMap = new ConcurrentHashMap<>();

        this.executor = executor;
        this.instanceIdentification = instanceIdentification;
        this.credentialsGenerator = credentialsGenerator;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void onOpen(InvocationContext invocationContext, Session session, EndpointConfig config) throws Exception {
        MultivaluedMap<String, String> headers = WebsocketUtil.getStoredObject(session, HttpHeaders.class.getSimpleName());
        WebsocketStateContext stateContext = createContext(invocationContext, session, headers);

        websocketStateContextMap.put(session.getId(), stateContext);
        WebsocketUtil.storeWebsocketContext(session, stateContext);

        WebsocketRequestDetails requestDetails = stateContext.newRequestDetailInstance();
        executor.executeChecked(requestDetails, () -> {
            boolean shouldDisconnect = authenticate(requestDetails, headers, getPermittedRoles(invocationContext));

            if (shouldDisconnect && isSecured(invocationContext)) {
                session.close();
            } else {
                invocationContext.proceed();
            }
        });
    }

    @Override
    public void onMessage(InvocationContext invocationContext, Session session) throws Exception {
        executor.executeChecked(websocketStateContextMap.get(session.getId()).newRequestDetailInstance(), invocationContext::proceed);
    }

    @Override
    public void onClose(InvocationContext invocationContext, Session session) throws Exception {
        WebsocketStateContext stateContext = websocketStateContextMap.remove(session.getId());
        executor.executeChecked(stateContext.newRequestDetailInstance(), invocationContext::proceed);
    }

    @Override
    public Collection<WebsocketStateContext> getActiveWebsocketStates() {
        return websocketStateContextMap.values();
    }

    protected WebsocketRequestDetails createUserRequestDetails(Session session, MultivaluedMap<String, String> headers) {
        return new WebsocketRequestDetails(
                getTraceId(),
                instanceIdentification.getInstanceId(),
                getRemoteAddress(headers),
                new WebsocketRequestDetails.Context(session.getRequestURI().toString()));
    }

    protected WebsocketStateContext createContext(InvocationContext invocationContext, Session session, MultivaluedMap<String, String> headers) {
        return new DefaultWebsocketStateContext(session, createUserRequestDetails(session, headers));
    }

    protected boolean authenticate(UserRequestDetails requestDetails, MultivaluedMap<String, String> headers, Set<String> permitted) {
        boolean failed = true;
        try {
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                return true;
            }

            Credential credential = credentialsGenerator.generate(authHeader);
            authenticationProvider.authenticate(requestDetails, credential);

            failed = !requestDetails.hasOneOfTheRoles(permitted);
        } catch (ExternalRuntimeException ignored) {}

        return failed;
    }

    protected String getTraceId() {
        return UUID.randomUUID().toString();
    }

    protected boolean isSecured(InvocationContext invocationContext) {
        return invocationContext.getMethod().getDeclaringClass().getAnnotation(NeoUtilWebsocket.class).secured();
    }

    protected Set<String> getPermittedRoles(InvocationContext invocationContext) {
        return Set.of(invocationContext.getMethod().getDeclaringClass().getAnnotation(NeoUtilWebsocket.class).roles());
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
}
