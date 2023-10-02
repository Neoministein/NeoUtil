package com.neo.util.framework.websocket.impl.security;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.websocket.impl.AbstractWebsocketIT;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

@HelidonTest
class AuthenticationWebsocketIT extends AbstractWebsocketIT {

    @Inject
    protected AuthenticatedWebsocket socketWithId;

    @BeforeEach
    void before() {
        socketWithId.getMessageMap().clear();
        socketWithId.setRoles(Set.of());
    }

    @Test
    void noHeaderTest() throws IOException {
        Session session = connectToWebsocket("/auth/id-1", val -> {});
        session.getBasicRemote().sendText("A message 1");

        ThreadUtils.simpleSleep(1000);

        Assertions.assertTrue(socketWithId.getMessageMap().isEmpty());
    }

    @Test
    void authenticationTest() throws IOException {
        Session session = connectToWebsocket("/auth/id-1", BasicAuthorizationProvider.NORMAL_TOKEN, val -> {});
        session.getBasicRemote().sendText("A message 1");

        IntegrationTestUtil.sleepUntil(100, 50, () -> {
            Assertions.assertFalse(socketWithId.getMessageMap().isEmpty());
            return true;
        });
    }

    @Test
    void authenticationFailureTest() throws IOException {
        Session session = connectToWebsocket("/auth/id1-","", val -> {});
        session.getBasicRemote().sendText("A message 1");

        ThreadUtils.simpleSleep(1000);

        Assertions.assertTrue(socketWithId.getMessageMap().isEmpty());
    }

    @Test
    void authorizationTest() throws IOException {
        socketWithId.setRoles(Set.of("ADMIN"));

        Session session = connectToWebsocket("/auth/id-1", BasicAuthorizationProvider.ADMIN_TOKEN, val -> {});
        session.getBasicRemote().sendText("A message 1");

        IntegrationTestUtil.sleepUntil(100, 50, () -> {
            Assertions.assertFalse(socketWithId.getMessageMap().isEmpty());
            return true;
        });
    }

    @Test
    void authorizationFailureTest() throws IOException {
        socketWithId.setRoles(Set.of("SUPER_ADMIN"));

        Session session = connectToWebsocket("/auth/id-1", BasicAuthorizationProvider.ADMIN_TOKEN, val -> {});
        session.getBasicRemote().sendText("A message 1");

        ThreadUtils.simpleSleep(1000);

        Assertions.assertTrue(socketWithId.getMessageMap().isEmpty());
    }
}
