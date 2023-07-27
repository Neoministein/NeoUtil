package com.neo.util.framework.websocket.impl.request;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.impl.AbstractWebsocketEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint(value = "/request/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class RequestWebsocket extends AbstractWebsocketEndpoint {


    protected RequestDetails requestDetails;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @Override
    public void onOpen(Session session) {
        requestDetails = requestDetailsProvider.get();
    }

    @Override
    public void onMessage(Session session, String message) {
        requestDetails = requestDetailsProvider.get();
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}