package com.neo.util.framework.api.queue;

public interface QueueListener {

    void onMessage(QueueMessage queueMessage);
}
