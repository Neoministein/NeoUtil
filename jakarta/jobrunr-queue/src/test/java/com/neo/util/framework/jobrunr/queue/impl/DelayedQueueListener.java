package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
@IncomingQueueConnection(DelayedQueueListener.QUEUE_NAME)
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
