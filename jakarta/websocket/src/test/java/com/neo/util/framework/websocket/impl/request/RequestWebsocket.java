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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NeoUtilWebsocket
@ApplicationScoped
@ServerEndpoint(value = "/request/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class RequestWebsocket implements BasicWebsocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestWebsocket.class);


    protected RequestDetails requestDetails;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        LOGGER.info("OnOpen");
        requestDetails = requestDetailsProvider.get();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        LOGGER.info("OnMessage");
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