package com.neo.util.framework.api.queue;

/**
 * This interface is designed for a CDI bean which has the {@link  IncomingQueue} annotation.
 * It hooks up the implemented onMessage method to the queues output of chosen implementation.
 * <p>
 * Each queue can only have one annotation and interface utilizing it per program
 */
public interface QueueListener {

    /**
     * Receives a queueMessage which was serialized from json after receiving it from the queue
     *
     * @param queueMessage the deserialized message from the queue
     */
    void onMessage(QueueMessage queueMessage);
}
