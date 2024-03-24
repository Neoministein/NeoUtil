package com.neo.util.framework.websocket.impl;

import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import com.neo.util.framework.websocket.api.scope.WebsocketScope;
import com.networknt.org.apache.commons.validator.routines.InetAddressValidator;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Set;
import java.util.UUID;

@WebsocketScope
public class WebsocketAccessController {

    public static final String X_REAL_IP = "X-Real-IP";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    protected static final String INVALID_IP = "255.255.255.255";

    @Inject
    protected InstanceIdentification instanceIdentification;

    @Inject
    protected HttpCredentialsGenerator httpCredentialsGenerator;

    @Inject
    protected AuthenticationProvider authenticationProvider;

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

            Credential credential = httpCredentialsGenerator.generate(authHeader);
            authenticationProvider.authenticate(requestDetails, credential);

            failed = !requestDetails.hasOneOfTheRoles(roles);
        } catch (ExternalRuntimeException ignored) {}

        return failed;
    }

    protected String getTraceId() {
        return UUID.randomUUID().toString();
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
