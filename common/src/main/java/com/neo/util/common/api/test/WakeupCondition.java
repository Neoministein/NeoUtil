package com.neo.util.common.api.test;


/**
 * A generic interface to check if a test should wake up.
 */

public interface WakeupCondition {

    /**
     * Returns true if the caller should wake up.
     */
    boolean shouldWakeUp();
}
