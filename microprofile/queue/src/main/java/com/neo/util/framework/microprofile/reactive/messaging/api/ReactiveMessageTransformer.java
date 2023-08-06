package com.neo.util.framework.microprofile.reactive.messaging.api;

import java.util.function.Function;

/**
 * Produces a function to transform an outgoing queue message
 */
public interface ReactiveMessageTransformer {

    default String getTransformerId() {
        return this.getClass().getSimpleName();
    }

    /**
     * Takes in the name of the queue and produces a function which transforms the outgoing message
     *
     * @param queueName the queue name
     * @return a function which takes in the queue message and returns the transformed message
     */
    Function<String, ?> getMessageTransformer(String queueName);
}
