package com.neo.util.framework.impl.queue;

import com.neo.util.framework.api.queue.QueueConfig;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@ApplicationScoped
@SuppressWarnings("java:S1186") //Default search implementation that does nothing
public class DummyQueueService implements QueueService {

    @Override
    public Collection<String> getQueueNames() {
        return Set.of();
    }

    @Override
    public void addToQueue(String queueName, Serializable payload) {

    }

    @Override
    public void addToQueue(String queueName, QueueMessage message) {

    }

    @Override
    public QueueConfig getQueueConfig(String queueName) {
        return null;
    }
}
