package com.neo.util.framework.api.queue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * This annotation specifies that an outgoing queue connection should be established with the provided configs.
 * <p>
 * If necessary a {@link QueueProducer} will be generated for the underling impl.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutgoingQueue {

    /**
     * The queue name
     */
    String value();

    /**
     * The Type of the Queue
     */
    QueueType type() default QueueType.QUEUE;

    /**
     * How many times it should be retried
     */
    int retry() default 0;

    /**
     * The delay after it as been queued before it will be sent to the listner
     */
    int delay() default 0;

    /**
     * Timeunit of the delay
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}