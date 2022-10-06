package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.RandomString;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
import com.neo.util.framework.microprofile.reactive.messaging.BasicQueueProducer; //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
import com.neo.util.framework.microprofile.reactive.messaging.BasicQueueConsumerCaller; //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueConsumer;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueService;
import io.helidon.messaging.connectors.mock.MockConnector;
import io.helidon.microprofile.messaging.MessagingCdiExtension;
import io.helidon.microprofile.tests.junit5.*;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.junit.jupiter.api.Test;


@HelidonTest
@DisableDiscovery
@AddExtension(MessagingCdiExtension.class)
@AddBean(RequestDetailsProducer.class)
@AddBean(MicroProfileQueueService.class)
@AddBean(BasicQueueConsumer.class)
@AddBean(BasicQueueService.class)
@AddBean(MockConnector.class)
@AddBean(BasicQueueProducer.class) //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
@AddBean(BasicQueueConsumerCaller.class) //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
public class BasicQueueFunctionalityIT {

    protected static final QueueMessage BASIC_QUEUE_MESSAGE = new QueueMessage("A_CALLER", new RandomString().nextString(),"A_TYPE", "A_PAYLOAD");

    @Inject
    BasicQueueService basicQueueService;

    @Inject
    BasicQueueConsumer basicQueueConsumer;

    @Incoming(OutgoingQueueConnectionProcessor.QUEUE_PREFIX + BasicQueueService.QUEUE_NAME)
    @Outgoing(IncomingQueueConnectionProcessor.QUEUE_PREFIX + BasicQueueService.QUEUE_NAME)
    public String internalQueueConector(String payload) {
        return payload;
    }

    @Test
    @SuppressWarnings("java:S2699") //IntegrationTestUtil.sleepUntil contains Assertions.fail
    void basicThroughPutTest() {
        basicQueueService.addToIndexingQueue(BASIC_QUEUE_MESSAGE);
        IntegrationTestUtil.sleepUntil(2000,10,() -> {
            if (basicQueueConsumer.getMessages().size() != 1) {
                return false;
            }
            QueueMessage messageFromQueue = basicQueueConsumer.getMessages().get(0);
            return BASIC_QUEUE_MESSAGE.getMessageType().equals(messageFromQueue.getMessageType())
                    && BASIC_QUEUE_MESSAGE.getPayload().equals(messageFromQueue.getPayload());
        });
    }
}