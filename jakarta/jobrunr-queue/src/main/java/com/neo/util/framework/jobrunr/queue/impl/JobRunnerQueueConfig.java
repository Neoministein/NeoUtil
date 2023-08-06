package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueConfig;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.QueueRequestDetails;

public class JobRunnerQueueConfig extends QueueConfig {

    protected final RequestContext requestContext;
    protected final QueueListener queueListener;

    public JobRunnerQueueConfig(Config config, OutgoingQueue outgoingConnection, QueueListener queueListener) {
        super(config, outgoingConnection);
        this.requestContext = new QueueRequestDetails.Context(queueName);
        this.queueListener = queueListener;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public QueueListener getQueueListener() {
        return queueListener;
    }
}
