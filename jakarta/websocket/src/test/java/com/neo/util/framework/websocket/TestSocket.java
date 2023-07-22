package com.neo.util.framework.websocket;

import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@InSession
@ApplicationScoped
@ServerEndpoint(value = "/websocket/{id}", configurator = CustomServerEndpointConfig.class)
public class TestSocket {

    @Inject
    TestBean testBean;

    @Context
    EndpointConfig config2;

    public TestSocket() {
        System.out.println("IDK");
    }

    @OnOpen
    public void onOpen(@PathParam("id") String id, Session session, EndpointConfig config) throws IOException {
        System.out.println(session);
        session.getPathParameters().get("id");

        Map<String, Object> test = session.getUserProperties();

        testBean.aMethod();
        session.getBasicRemote().sendText("pong");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println(message);

    }

    @OnClose
    public void onClose(Session session) {
        System.out.println(session);
        testBean.aMethod();
    }
}