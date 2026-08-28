package com.neo.util.framework.api.queue;

import com.neo.util.framework.api.config.ConfigService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This class holds implementation independent config values for a queue.
 */
public class QueueConfig {

    protected final String queueName;
    protected final int retry;
    protected final int delay;
    protected final TimeUnit timeUnit;

    public QueueConfig(ConfigService config, OutgoingQueue outgoingConnection) {
        this.queueName  = outgoingConnection.value();
        this.retry      = config.getAsInt("queue",queueName, "retry").orElse(outgoingConnection.retry());
        this.delay      = config.getAsInt("queue", queueName, "delay").orElse(outgoingConnection.delay());
        this.timeUnit   = config.getAsString("queue", queueName,"time-unit").map(TimeUnit::valueOf).orElse(outgoingConnection.timeUnit());
    }

    public QueueConfig(String queueName, int retry, int delay, TimeUnit timeUnit) {
        this.queueName = queueName;
        this.retry = retry;
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getRetry() {
        return retry;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public int getDelay() {
        return delay;
    }

    public Duration getDuration() {
        return Duration.ofSeconds(getTimeUnit().toSeconds(getDelay()));
    }
}
