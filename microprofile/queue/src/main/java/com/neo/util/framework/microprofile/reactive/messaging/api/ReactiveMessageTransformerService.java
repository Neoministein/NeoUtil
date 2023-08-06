package com.neo.util.framework.microprofile.reactive.messaging.api;

/**
 * Manages {@link ReactiveMessageTransformer} and produces a function to transform an outgoing queue message
 */
public interface ReactiveMessageTransformerService {

    /**
     * Takes in the name of the queue and produces the relevant transformer
     *
     * @param queueName the queue name
     * @return the relevant transformer
     */
    ReactiveMessageTransformer getTransformer(String queueName);
}
