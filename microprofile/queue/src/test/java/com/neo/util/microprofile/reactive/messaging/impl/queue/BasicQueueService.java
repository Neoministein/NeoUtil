package com.neo.util.microprofile.reactive.messaging.impl.queue;

import com.neo.util.api.queue.OutgoingQueue;
import com.neo.util.api.queue.QueueMessage;
import com.neo.util.api.queue.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@OutgoingQueue(BasicQueueService.QUEUE_NAME)
public class BasicQueueService {

    public static final String QUEUE_NAME = "basicQueue";

    @Inject
    protected QueueService queueService;

    /**
     * put message to indexer queue.
     *
     * @param queueMessage
     *            the message
     */
    public void addToIndexingQueue(QueueMessage queueMessage) {
        queueService.addToQueue(QUEUE_NAME, queueMessage);
    }
}
