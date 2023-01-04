package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.RandomString;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.impl.RequestContextExecutor;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
import com.neo.util.framework.microprofile.reactive.messaging.RequestQueueProducer; //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
import com.neo.util.framework.microprofile.reactive.messaging.RequestQueueConsumerCaller; //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.RequestQueueConsumer;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.RequestQueueService;
import io.helidon.messaging.connectors.mock.MockConnector;
import io.helidon.microprofile.messaging.MessagingCdiExtension;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.AddExtension;
import io.helidon.microprofile.tests.junit5.DisableDiscovery;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.junit.jupiter.api.Test;

@HelidonTest
@DisableDiscovery
@AddExtension(MessagingCdiExtension.class)
@AddBean(RequestDetailsProducer.class)
@AddBean(MicroProfileQueueService.class)
@AddBean(RequestContextExecutor.class)
@AddBean(RequestQueueService.class)
@AddBean(RequestQueueConsumer.class)
@AddBean(MockConnector.class)
@AddBean(RequestQueueProducer.class) //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
@AddBean(RequestQueueConsumerCaller.class) //IMPORTANT: IDE WON'T REFERENCE BUT IT IS COMPILABLE IN MAVEN AND INTELLIJ
@AddBean(value = BasicRequestScopedBean.class, scope = RequestScoped.class)
public class NonFailingRequestContextQueueIT {

    protected static final QueueMessage BASIC_QUEUE_MESSAGE = new QueueMessage("A_CALLER", new RandomString().nextString(),"A_TYPE", "A_PAYLOAD");

    @Inject
    RequestQueueService requestQueueService;

    @Inject
    RequestQueueConsumer requestQueueConsumer;

    @Incoming(OutgoingQueueConnectionProcessor.QUEUE_PREFIX + RequestQueueService.QUEUE_NAME)
    @Outgoing(IncomingQueueConnectionProcessor.QUEUE_PREFIX + RequestQueueService.QUEUE_NAME)
    public String internalQueueConector(String payload) {
        return payload;
    }

    @Test
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