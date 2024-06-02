package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueConsumer;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueService;
import io.helidon.microprofile.messaging.MessagingCdiExtension;
import io.helidon.microprofile.testing.junit5.AddExtension;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@HelidonTest
@AddExtension(MessagingCdiExtension.class)
class BasicQueueFunctionalityIT {

    protected static final QueueMessage BASIC_QUEUE_MESSAGE = new QueueMessage("A_CALLER","A_TRACE_ID","A_TYPE", "A_PAYLOAD");

    @Inject
    BasicQueueService basicQueueService;

    @Inject
    BasicQueueConsumer basicQueueConsumer;



    @Test
    @SuppressWarnings("java:S2699") //IntegrationTestUtil.sleepUntil contains Assertions.fail
    void basicThroughPutTest() {
        basicQueueService.addToIndexingQueue(BASIC_QUEUE_MESSAGE);
        IntegrationTestUtil.sleepUntil(2000,10,() -> {
            Assertions.assertEquals(1, basicQueueConsumer.getMessages().size());

            QueueMessage messageFromQueue = basicQueueConsumer.getMessages().get(0);
            Assertions.assertEquals(BASIC_QUEUE_MESSAGE.getMessageType(), messageFromQueue.getMessageType());
            Assertions.assertEquals(BASIC_QUEUE_MESSAGE.getPayload(), messageFromQueue.getPayload());
        });
    }
}