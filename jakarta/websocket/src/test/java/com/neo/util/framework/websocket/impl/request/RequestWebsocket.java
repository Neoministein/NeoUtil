package com.neo.util.framework.websocket.impl.request;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.websocket.api.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.impl.BasicWebsocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@NeoUtilWebsocket
@ApplicationScoped
@ServerEndpoint(value = "/request/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class RequestWebsocket implements BasicWebsocket {


    protected RequestDetails requestDetails;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        requestDetails = requestDetailsProvider.get();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        requestDetails = requestDetailsProvider.get();
    }

    @OnClose
    public void onClose(Session session) {}

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}