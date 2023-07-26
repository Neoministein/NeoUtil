package com.neo.util.framework.websocket.impl;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class WebsocketAccessController {

    @Inject
    protected InstanceIdentification instanceIdentification;

    @Inject
    protected CredentialsGenerator credentialsGenerator;

    @Inject
    protected AuthenticationProvider authenticationProvider;

    public UserRequestDetails createUserRequestDetails(Session session) {
        return new WebsocketRequestDetails(
                getTraceId(),
                instanceIdentification.getInstanceId(),
                new WebsocketRequestDetails.Context(session.getRequestURI().toString()));
    }
    
    public boolean authenticate(UserRequestDetails requestDetails, Map<String, List<String>> headers, boolean secured, Set<String> roles) {
        if (!secured) {
            return false;
        }

        boolean shouldDisconnect;
        try {
            List<String> authHeader = headers.get(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                return true;
            }

            Credential credential = credentialsGenerator.generate(authHeader.stream().findFirst().orElse(StringUtils.EMPTY));
            authenticationProvider.authenticate(requestDetails, credential);

            shouldDisconnect = !requestDetails.hasOneOfTheRoles(roles);
        } catch (CommonRuntimeException ex) {
            shouldDisconnect = true;
        }

        return shouldDisconnect;
    }

    protected String getTraceId() {
        return UUID.randomUUID().toString();
    }
}
