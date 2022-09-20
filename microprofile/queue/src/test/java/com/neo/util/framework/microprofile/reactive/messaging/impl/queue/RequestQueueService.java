package com.neo.util.framework.microprofile.reactive.messaging.impl.queue;

import com.neo.util.framework.api.queue.OutgoingQueueConnection;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@OutgoingQueueConnection(RequestQueueService.QUEUE_NAME)
public class RequestQueueService {

    public static final String QUEUE_NAME = "requestQueue";

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
