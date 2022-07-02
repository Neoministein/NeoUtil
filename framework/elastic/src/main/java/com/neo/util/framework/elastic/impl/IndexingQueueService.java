package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.queue.OutgoingQueueConnection;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@OutgoingQueueConnection(IndexingQueueService.QUEUE_NAME)
public class IndexingQueueService {

    public static final String QUEUE_NAME = "indexingQueue";

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
