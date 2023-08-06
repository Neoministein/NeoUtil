package com.neo.util.framework.api.queue;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can only be placed on a CDI bean which implements the interface {@link  QueueListener}.
 * It hooks up the implemented onMessage method to the queues output of chosen implementation.
 * <p>
 * Each queue can only have one annotation and interface utilizing it per program
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IncomingQueue {

    /**
     * The queue name
     */
    String value();
}
