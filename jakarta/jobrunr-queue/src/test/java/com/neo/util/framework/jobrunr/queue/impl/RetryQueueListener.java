package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.queue.IncomingQueue;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IncomingQueue(RetryQueueListener.QUEUE_NAME)
@OutgoingQueue(RetryQueueListener.QUEUE_NAME)
public class RetryQueueListener implements QueueListener {

    public static final String QUEUE_NAME = "retry";

    private boolean first = true;
    private QueueMessage lastMessage;

    @Override
    public void onMessage(QueueMessage queueMessage) {
        if (first) {
            first = false;
            throw new RuntimeException();
        }
        lastMessage = queueMessage;
    }

    public boolean isFirst() {
        return first;
    }

    public QueueMessage getLastMessage() {
        return lastMessage;
    }
}
