package com.neo.util.framework.api.connection;

/**
 * This interface consolidates all the data for a single request by a queue.
 */
public interface QueueRequestDetails extends RequestDetails {

    /**
     * The name of the queue
     */
    String getQueueName();

    @Override
    default String getCaller() {
        return getQueueName();
    }
}
