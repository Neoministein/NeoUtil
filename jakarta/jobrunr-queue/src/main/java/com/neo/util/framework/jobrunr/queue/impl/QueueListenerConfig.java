package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.impl.request.QueueRequestDetails;

public class QueueListenerConfig {

    protected final String queueName;
    protected final RequestContext requestContext;
    protected final int delayInSeconds;
    protected final QueueListener queueListener;

    public QueueListenerConfig(String queueName, int delayInSeconds, QueueListener queueListener) {
        this.queueName = queueName;
        this.requestContext = new QueueRequestDetails.Context(queueName);
        this.delayInSeconds = delayInSeconds;
        this.queueListener = queueListener;
    }

    public String getQueueName() {
        return queueName;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public boolean hasDelay() {
        return delayInSeconds > 0;
    }

    public int getDelayInSeconds() {
        return delayInSeconds;
    }

    public QueueListener getQueueListener() {
        return queueListener;
    }
}
