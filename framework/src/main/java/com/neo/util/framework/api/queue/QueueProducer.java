package com.neo.util.framework.api.queue;

/**
 * Defines an outgoing connection to a queue.
 *
 * It can be generated by an {@link OutgoingQueue}
 */
public interface QueueProducer {

    /**
     * Receives an {@link QueueMessage} as a json serialized {@link String}.
     * <p>
     * It enqueues the messages as ObjectMessage into the given Queue using the non-strictly ordered connection factory.
     * <p>
     * The utility method is here to be used for the most commonly used cases to send notification messages to other
     * processes.
     */
    void addToQueue(String message);

    /**
     * The name of the queue
     *
     * @return the name of the queue which it connects to
     */
    String getQueueName();
}
