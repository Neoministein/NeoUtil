package com.neo.util.framework.api.connection;

/**
 * This interface consolidates all the data for a single request by a timer.
 */
public interface TimerRequestDetails extends RequestDetails {

    /**
     * The name of the timer
     */
    String getTimerName();

    @Override
    default String getCaller() {
        return getTimerName();
    }
}
