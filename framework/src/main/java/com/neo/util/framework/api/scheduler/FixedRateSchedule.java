package com.neo.util.framework.api.scheduler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Scheduled to be invoked periodically at fixed rate.
 * Value is interpreted as seconds by default, can be overridden by {@link #timeUnit()}.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface FixedRateSchedule {

    /**
     * An application wide unique identifier for this schedule
     */
    String id();

    /**
     * Fixed rate for periodical invocation.
     */
    long value();

    //TODO IMPL DEALY

    /**
     * Time unit for interpreting supplied values.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}