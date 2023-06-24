package com.neo.util.framework.api.queue;

import com.neo.util.common.impl.exception.ExceptionDetails;

/**
 * This interface is an abstraction to the different Queue implementations in Jakarta, Helidon and Quarkus.
 * It defines the necessary functionality which all 3 should implement in one standardized interface.
 */
public interface QueueService {


    ExceptionDetails EX_DUPLICATED_QUEUE = new ExceptionDetails(
            "queue/duplicated-queue-configured","Duplicated queues present [{0}] [{1}]",true);

    ExceptionDetails EX_NON_EXISTENT_QUEUE = new ExceptionDetails(
            "queue/non-existent-queue", "The [{0}] [{1}] does not exist", true);

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