package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.queue.QueueConfig;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.QueueRequestDetails;

public class JobRunnerQueueConfig {

    protected final QueueConfig config;
    protected final RequestContext requestContext;
    protected final QueueListener queueListener;

    public JobRunnerQueueConfig(QueueConfig config, QueueListener queueListener) {
        this.config = config;
        this.queueListener = queueListener;
        this.requestContext = new QueueRequestDetails.Context(config.getQueueName());
    }

    public QueueConfig getConfig() {
        return config;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public QueueListener getQueueListener() {
        return queueListener;
    }
}
