package com.neo.util.framework.websocket.impl.request;

import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.websocket.impl.AbstractWebsocketIT;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@HelidonTest
class RequestWebsocketIT extends AbstractWebsocketIT {

    @Inject
    protected RequestWebsocket requestWebsocket;

    @BeforeEach
    void before() {
        requestWebsocket.setRequestDetails(null);
    }

    @Test
    void test() throws IOException {
        Session session_1 = connectToWebsocket("/request/id-1", val -> {});
        IntegrationTestUtil.sleepUntil(() -> assertRequestDetails(requestWebsocket.getRequestDetails(), "/request/id-1"));

        Session session_2 = connectToWebsocket("/request/id-2", val -> {});
        IntegrationTestUtil.sleepUntil(() -> assertRequestDetails(requestWebsocket.getRequestDetails(), "/request/id-2"));

        session_1.getBasicRemote().sendText("A message 1");
        IntegrationTestUtil.sleepUntil(() -> assertRequestDetails(requestWebsocket.getRequestDetails(), "/request/id-1"));
        session_2.getBasicRemote().sendText("A message 1");
        IntegrationTestUtil.sleepUntil(() -> assertRequestDetails(requestWebsocket.getRequestDetails(), "/request/id-2"));

        session_1.close();
        session_2.close();
    }

    protected boolean assertRequestDetails(RequestDetails requestDetails, String context) {
        Assertions.assertNotNull(requestDetails);
        Assertions.assertEquals(requestDetails.getRequestContext().toString(), context);
        return true;
    }
}
