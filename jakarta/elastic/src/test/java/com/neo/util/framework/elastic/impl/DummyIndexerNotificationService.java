package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.queue.QueueMessage;

public class DummyIndexerNotificationService extends IndexingQueueService {

    protected QueueMessage queueMessage;

    public DummyIndexerNotificationService() {
        super(null);
    }

    @Override
    public void addToIndexingQueue(QueueMessage queueMessage) {
        this.queueMessage = queueMessage;
    }

    public QueueMessage getQueueMessage() {
        return queueMessage;
    }

    public void reset() {
        this.queueMessage = null;
    }

}
