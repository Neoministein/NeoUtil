package com.neo.util.framework.microprofile.reactive.messaging.api;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueConfig;
import com.neo.util.framework.api.queue.QueueProducer;

public class MicroProfileQueueConfig extends QueueConfig {

    protected final QueueProducer queueProducer;

    public MicroProfileQueueConfig(Config config, OutgoingQueue outgoingConnection, QueueProducer queueProducer) {
        super(config, outgoingConnection);
        this.queueProducer = queueProducer;
    }

    public QueueProducer getQueueProducer() {
        return queueProducer;
    }
}
