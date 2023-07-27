package com.neo.util.framework.websocket.impl.monitoring;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.framework.impl.persistence.search.DummySearchProvider;
import com.neo.util.framework.websocket.impl.AbstractWebsocketIT;
import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@HelidonTest
class MonitoringWebsocketIT extends AbstractWebsocketIT {

    @Inject
    protected MonitorableWebsocketScheduler monitorableWebsocketScheduler;

    @Inject
    protected DummySearchProvider dummySearchProvider;

    @Test
    void test() throws IOException {
        String message = "A message 1";

        Session session_1 = connectToWebsocket("/monitoring/id-1", val -> {});
        session_1.getBasicRemote().sendText(message);
        ThreadUtils.simpleSleep(1000);
        monitorableWebsocketScheduler.action();

        SocketLogSearchable searchable = dummySearchProvider.getSearchableToIndex();
        Assertions.assertNotNull(searchable);
        Assertions.assertEquals(message.length(), searchable.getIncoming());
        Assertions.assertEquals(message.length() + 1, searchable.getOutgoing());
        Assertions.assertEquals("/monitoring/id-1", searchable.getContext());
        session_1.close();
    }
}
