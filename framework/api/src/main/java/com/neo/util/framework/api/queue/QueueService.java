package com.neo.util.framework.api.queue;

/**
 * This interface is an abstraction to the different Queue implementations in Jakarta, Helidon and Quarkus.
 * It defines the necessary functionality which all 3 should implement in one standardized interface.
 */
public interface QueueService {

    /**
     * Enqueues the messages as ObjectMessage into the given Queue using the non-strictly ordered connection factory.
     * <p>
     * The utility method is here to be used for the most commonly used cases to send notification messages to other
     * processes.
     * <p>
     * Optionally String properties can be provided.
     */
    void addToQueue(String queueName, QueueMessage message);
}