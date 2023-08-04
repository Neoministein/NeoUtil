package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.QueueRequestDetails;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class QueueListenerConfig {

    protected final String queueName;
    protected final RequestContext requestContext;
    protected final int retry;
    protected final int delay;
    protected final TimeUnit timeUnit;
    protected final QueueListener queueListener;

    public QueueListenerConfig(String queueName, int retry, int delay, TimeUnit timeUnit, QueueListener queueListener) {
        this.queueName = queueName;
        this.requestContext = new QueueRequestDetails.Context(queueName);
        this.retry = retry;
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.queueListener = queueListener;
    }

    public String getQueueName() {
        return queueName;
    }

    public RequestContext getRequestContext() {
        return requestContext;
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

    public QueueListener getQueueListener() {
        return queueListener;
    }
}
