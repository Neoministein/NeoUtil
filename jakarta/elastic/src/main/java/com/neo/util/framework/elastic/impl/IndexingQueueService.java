package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
@OutgoingQueue(value = IndexingQueueService.QUEUE_NAME, retry = 12, delay = 10, timeUnit = TimeUnit.MINUTES)
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
