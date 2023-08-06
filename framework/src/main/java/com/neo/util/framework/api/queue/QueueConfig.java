package com.neo.util.framework.api.queue;

import com.neo.util.framework.api.config.Config;

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

    public QueueConfig(Config config, OutgoingQueue outgoingConnection) {
        Config queueConfig = config.get(outgoingConnection.value());
        this.queueName  = outgoingConnection.value();
        this.retry      = queueConfig.get("retry").asInt().orElse(outgoingConnection.retry());
        this.delay      = queueConfig.get("delay").asInt().orElse(outgoingConnection.delay());
        this.timeUnit   = queueConfig.get("time-unit").asString().map(TimeUnit::valueOf).orElse(outgoingConnection.timeUnit());
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
