package com.neo.util.framework.microprofile.reactive.messaging.api;

import com.neo.util.framework.api.queue.QueueConfig;
import com.neo.util.framework.api.queue.QueueProducer;

public class MicroProfileQueueConfig {

    protected final QueueConfig queueConfig;
    protected final QueueProducer queueProducer;

    public MicroProfileQueueConfig(QueueConfig queueConfig, QueueProducer queueProducer) {
        this.queueConfig = queueConfig;
        this.queueProducer = queueProducer;
    }

    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    public QueueProducer getQueueProducer() {
        return queueProducer;
    }
}
