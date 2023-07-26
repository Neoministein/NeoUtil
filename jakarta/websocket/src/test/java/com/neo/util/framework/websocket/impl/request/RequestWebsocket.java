package com.neo.util.framework.websocket.impl.request;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.impl.AbstractWebsocketEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Provider;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

@ApplicationScoped
@ServerEndpoint(value = "/request/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class RequestWebsocket extends AbstractWebsocketEndpoint {


    protected RequestDetails requestDetails;

    protected Provider<RequestDetails> requestDetailsProvider;

    @Override
    protected boolean secured() {
        return false;
    }

    @Override
    public void onOpen(Session session) throws IOException {
        requestDetails.getRequestContext();
    }

    @Override
    public void onMessage(Session session, String message) {
        String id = getPathParameter(session, "id");
    }
}