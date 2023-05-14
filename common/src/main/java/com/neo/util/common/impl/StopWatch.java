package com.neo.util.common.impl;

import java.time.Duration;
import java.time.Instant;

/**
 * A very basic class to time elapsed time between a start and a stop call.
 */
public final class StopWatch {

    /**
     * Start date of when the stop watch was started.
     */
    Instant start = null;
    /**
     * Stop date of when the stop watch was started.
     */
    Instant stop = null;

    /**
     * sets the start date and resets the stop date.
     */
    public StopWatch start() {
        start = Instant.now();
        stop = null;
        return this;
    }

    /**
     * sets the stop date.
     */
    public void stop() {
        stop = Instant.now();
    }

    /**
     * Get the time elapsed between the call to start and stop.
     *
     * @return the time passed
     */
    public Duration getElapsedTime() {
        // (a) if the user is not using the apis properly the value he gets will be a dummy one
        if (start == null || stop == null) {
            throw new IllegalStateException("StopWatch hasn't been started or stopped");
        }

        // (b) compute the time difference
        return Duration.between(start, stop);
    }

    /**
     * The elapsed time between start and stop call in ms.
     *
     * @return the elapsed time in ms.
     */
    public long getElapsedTimeMs() {
        return getElapsedTime().toMillis();
    }
}
