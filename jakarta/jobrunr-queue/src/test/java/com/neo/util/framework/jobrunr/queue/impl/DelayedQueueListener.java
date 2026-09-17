package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.api.queue.IncomingQueue;
import com.neo.util.api.queue.OutgoingQueue;
import com.neo.util.api.queue.QueueListener;
import com.neo.util.api.queue.QueueMessage;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IncomingQueue(DelayedQueueListener.QUEUE_NAME)
@OutgoingQueue(DelayedQueueListener.QUEUE_NAME)
public class DelayedQueueListener implements QueueListener {

    public static final String QUEUE_NAME = "delayedQueue";

    private QueueMessage lastMessage;

    @Override
    public void onMessage(QueueMessage queueMessage) {
        lastMessage = queueMessage;
    }

    public QueueMessage getLastMessage() {
        return lastMessage;
    }
}
