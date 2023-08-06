package com.neo.util.framework.microprofile.reactive.messaging.impl.queue;

import com.neo.util.framework.api.queue.IncomingQueue;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@IncomingQueue(BasicQueueService.QUEUE_NAME)
public class BasicQueueConsumer implements QueueListener {

    protected List<QueueMessage> messages = new ArrayList<>();

    @Override
    public void onMessage(QueueMessage message) {
        messages.add(message);
    }

    public List<QueueMessage> getMessages() {
        return messages;
    }
}
