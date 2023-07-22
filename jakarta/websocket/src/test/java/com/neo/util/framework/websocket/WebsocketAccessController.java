package com.neo.util.framework.websocket;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;
import jakarta.inject.Inject;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

public class WebsocketAccessController {

    @Inject
    protected CredentialsGenerator credentialsGenerator;

    @Inject
    protected AuthenticationProvider authenticationProvider;
    
    public void authenticate(Session session) {
        WebsocketRequestDetails websocketRequestDetails;

        try {

        } catch (CommonRuntimeException ex) {

        }
    }
}
