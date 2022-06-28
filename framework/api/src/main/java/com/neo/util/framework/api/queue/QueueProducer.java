package com.neo.util.framework.api.queue;

public interface QueueProducer {

    /**
     * Enqueues the messages as ObjectMessage into the given Queue using the non-strictly ordered connection factory.
     * <p>
     * The utility method is here to be used for the most commonly used cases to send notification messages to other
     * processes.
     */
    void addToQueue(String message);

    String getQueueName();
}
