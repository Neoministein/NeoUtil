package com.neo.util.framework.websocket.impl;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class WebsocketAccessController {

    @Inject
    protected InstanceIdentification instanceIdentification;

    @Inject
    protected CredentialsGenerator credentialsGenerator;

    @Inject
    protected AuthenticationProvider authenticationProvider;
    
    public boolean authenticate(Session session, EndpointConfig config, boolean secured, Set<String> roles) {
        Map<String, Object> userProperties = config.getUserProperties();

        UserRequestDetails websocketRequestDetails = createUserRequestDetails(session);

        userProperties.put(RequestDetails.class.getSimpleName(), websocketRequestDetails);
        if (!secured) {
            return false;
        }

        boolean shouldDisconnect;
        try {
            Credential credential = credentialsGenerator.generate(StringUtils.toString(userProperties.get(HttpHeaders.AUTHORIZATION)));
            authenticationProvider.authenticate(websocketRequestDetails, credential);

            shouldDisconnect = !websocketRequestDetails.hasOneOfTheRoles(roles);
        } catch (CommonRuntimeException ex) {
            shouldDisconnect = true;
        }

        return shouldDisconnect;
    }

    protected UserRequestDetails createUserRequestDetails(Session session) {
        return new WebsocketRequestDetails(instanceIdentification.getInstanceId(),
                new WebsocketRequestDetails.Context(session.getRequestURI().toString()));
    }
}
