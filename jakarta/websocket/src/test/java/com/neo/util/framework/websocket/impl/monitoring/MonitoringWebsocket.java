package com.neo.util.framework.websocket.impl.monitoring;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

@ApplicationScoped
@ServerEndpoint(value = "/monitoring/{id}", configurator = WebserverHttpHeaderForwarding.class)
public class MonitoringWebsocket extends AbstractMonitorableWebsocket {


    protected RequestDetails requestDetails;

    @Override
    public void onOpen(Session session) {}

    @Override
    public void onMessage(Session session, String message) throws IOException {
        super.onMessage(session, message);
        broadcast(session, message + "1");
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}