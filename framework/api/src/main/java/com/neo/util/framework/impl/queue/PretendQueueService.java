package com.neo.util.framework.impl.queue;

import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SuppressWarnings("java:S1186") //Default search implementation that does nothing
public class PretendQueueService implements QueueService {

    @Override
    public void addToQueue(String queueName, QueueMessage message) {

    }
}
