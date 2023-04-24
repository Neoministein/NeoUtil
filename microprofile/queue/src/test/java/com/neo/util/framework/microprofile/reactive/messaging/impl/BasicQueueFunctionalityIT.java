package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.RandomString;
import com.neo.util.framework.impl.RequestContextExecutor;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
//import com.neo.util.framework.microprofile.reactive.messaging.BasicQueueProducer; //Uncomment for test execution
//import com.neo.util.framework.microprofile.reactive.messaging.BasicQueueConsumerCaller; //Uncomment for test execution
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.microprofile.reactive.messaging.build.IncomingQueueConnectionProcessor;
import com.neo.util.framework.microprofile.reactive.messaging.build.OutgoingQueueConnectionProcessor;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueConsumer;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueService;
import io.helidon.messaging.connectors.mock.MockConnector;
import io.helidon.microprofile.messaging.MessagingCdiExtension;
import io.helidon.microprofile.tests.junit5.*;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;


@HelidonTest
@DisableDiscovery
@AddExtension(MessagingCdiExtension.class)
@AddBean(RequestDetailsProducer.class)
@AddBean(MicroProfileQueueService.class)
@AddBean(RequestContextExecutor.class)
@AddBean(BasicQueueConsumer.class)
@AddBean(BasicQueueService.class)
@AddBean(MockConnector.class)
//AddBean(BasicQueueProducer.class) //Uncomment for test execution
//@AddBean(BasicQueueConsumerCaller.class) //Uncomment for test execution
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

    //@Test
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