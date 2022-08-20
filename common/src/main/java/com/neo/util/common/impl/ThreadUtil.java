package com.neo.util.common.impl;

import com.neo.util.common.api.test.WakeupCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * Utilities for {@link Thread}
 */
public final class ThreadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtil.class);



    private ThreadUtil() {}


    @SuppressWarnings("java:S2142")
    public static void simpleSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * This method waits for the {@link WakeupCondition} to be true. <p>
     * If the max number of tries are exceeded, either false is returned or the conditions last exception is rethrown.
     */
    public static boolean fluentWait(int millisToSleep, int maxTries, WakeupCondition wakeUpCondition) {
        RuntimeException runtimeException = null;
        AssertionError assertionError = null;

        for (int tryCount = 0; tryCount < maxTries; tryCount++) {
            runtimeException = null;
            assertionError = null;

            boolean conditionMet = false;
            try {
                conditionMet = wakeUpCondition.shouldWakeUp();
            } catch (RuntimeException e) {
                runtimeException = e;
                LOGGER.warn("FluentWait - Exception occurred during wake up condition. Current: try [{}] in WakeupCondition [{}]. Exception message was: [{}] [{}]",
                        tryCount, wakeUpCondition, e.getClass().getSimpleName(), e.getMessage());
            } catch (AssertionError e) {
                assertionError = e;
                LOGGER.warn("FluentWait - Exception occurred during wake up condition. Current: try [{}] in WakeupCondition [{}]. Exception message was: [{}] [{}]",
                        tryCount, wakeUpCondition, e.getClass().getSimpleName(), e.getMessage());
            }

            if (conditionMet) {
                String totalTime = new SimpleDateFormat("HH:mm:ss.SSS").format(millisToSleep * tryCount);
                LOGGER.info("FluentWait - WakeupCondition [{}] met after [{}] tries, total time [{}]",
                        wakeUpCondition, tryCount, totalTime);
                return true;
            }

            ThreadUtil.simpleSleep(millisToSleep);
        }

        if (runtimeException != null) {
            throw runtimeException;
        }
        if (assertionError != null) {
            throw assertionError;
        }

        LOGGER.warn("FluentWait - no more tries left");
        return false;
    }
}