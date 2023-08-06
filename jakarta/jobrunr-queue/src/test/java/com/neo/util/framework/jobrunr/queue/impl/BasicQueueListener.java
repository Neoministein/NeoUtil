package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.queue.IncomingQueue;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IncomingQueue(BasicQueueListener.QUEUE_NAME)
@OutgoingQueue(BasicQueueListener.QUEUE_NAME)
public class BasicQueueListener implements QueueListener {

    public static final String QUEUE_NAME = "basicQueue";

    private QueueMessage lastMessage;

    @Override
    public void onMessage(QueueMessage queueMessage) {
        lastMessage = queueMessage;
    }

    public QueueMessage getLastMessage() {
        return lastMessage;
    }
}
