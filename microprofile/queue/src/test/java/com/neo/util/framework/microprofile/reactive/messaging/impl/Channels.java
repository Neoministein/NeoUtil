package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.framework.microprofile.reactive.messaging.build.IncomingQueueConnectionProcessor;
import com.neo.util.framework.microprofile.reactive.messaging.build.OutgoingQueueConnectionProcessor;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.BasicQueueService;
import com.neo.util.framework.microprofile.reactive.messaging.impl.queue.RequestQueueService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class Channels {

    @Incoming(OutgoingQueueConnectionProcessor.QUEUE_PREFIX + BasicQueueService.QUEUE_NAME)
    @Outgoing(IncomingQueueConnectionProcessor.QUEUE_PREFIX + BasicQueueService.QUEUE_NAME)
    public String internalBasicQueueConnector(String payload) {
        return payload;
    }

    @Incoming(OutgoingQueueConnectionProcessor.QUEUE_PREFIX + RequestQueueService.QUEUE_NAME)
    @Outgoing(IncomingQueueConnectionProcessor.QUEUE_PREFIX + RequestQueueService.QUEUE_NAME)
    public String internalRequestQueueConnector(String payload) {
        return payload;
    }
}