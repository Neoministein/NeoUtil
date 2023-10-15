package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.RequestQueueConsumer;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.RequestQueueService;
import io.helidon.microprofile.messaging.MessagingCdiExtension;
import io.helidon.microprofile.testing.junit5.AddExtension;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@HelidonTest
@AddExtension(MessagingCdiExtension.class)
class NonFailingRequestContextQueueIT {

    protected static final QueueMessage BASIC_QUEUE_MESSAGE = new QueueMessage("A_CALLER", "A_TRACE_ID","A_TYPE", "A_PAYLOAD");

    @Inject
    RequestQueueService requestQueueService;

    @Inject
    RequestQueueConsumer requestQueueConsumer;

    @Test
    @SuppressWarnings("java:S2699") //IntegrationTestUtil.sleepUntil contains Assertions.fail
    void test() {
        requestQueueService.addToIndexingQueue(BASIC_QUEUE_MESSAGE);
        IntegrationTestUtil.sleepUntil(2000,10,() -> {
            if (requestQueueConsumer.getMessages().size() != 1) {
                return false;
            }
            QueueMessage messageFromQueue = requestQueueConsumer.getMessages().get(0);
            return BASIC_QUEUE_MESSAGE.getMessageType().equals(messageFromQueue.getMessageType())
                    && BASIC_QUEUE_MESSAGE.getPayload().equals(messageFromQueue.getPayload());
        });
    }
}