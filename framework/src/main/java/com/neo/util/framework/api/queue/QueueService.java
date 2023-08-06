package com.neo.util.framework.api.queue;

import com.neo.util.common.impl.exception.ExceptionDetails;

import java.io.Serializable;
import java.util.Collection;

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
     * Returns a collection of all queue names.
     */
    Collection<String> getQueueNames();

    /**
     * The utility method is here to be used for the most commonly used cases to send notification messages to other
     * processes.
     */
    void addToQueue(String queueName, Serializable payload);

    /**
     * Enqueues the messages as a {@link QueueMessage} into the given Queue.
     */
    void addToQueue(String queueName, QueueMessage message);

    /**
     * Returns the config for the provided queue name
     */
    QueueConfig getQueueConfig(String queueName);
}