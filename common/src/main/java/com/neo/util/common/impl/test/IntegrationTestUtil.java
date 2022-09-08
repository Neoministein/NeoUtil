package com.neo.util.common.impl.test;

import com.neo.util.common.api.test.WakeupCondition;
import com.neo.util.common.impl.ThreadUtil;

import org.junit.jupiter.api.Assertions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility for Integration Tests
 */
public final class IntegrationTestUtil {

    private IntegrationTestUtil() {}

    /**
     * This method waits for the {@link WakeupCondition} to be true. <p>
     * If the max number of tries are exceeded, {@link  Assertions#fail()} will be invoked.
     */
    public static void sleepUntil(int millisToSleep, int tries, WakeupCondition wakeUpCondition) {
        String exceptionMessage = "No exception occurred";

        try {
            if (ThreadUtil.fluentWait(millisToSleep, tries, wakeUpCondition)) {
                return;
            }
        } catch (RuntimeException | AssertionError e) {
            exceptionMessage = e.getMessage();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        int totalTime = millisToSleep * tries;
        Assertions.fail("WakeupCondition [" + wakeUpCondition + "] not fulfilled after [" + tries + "] tries, total time ["
                + dateFormat.format(totalTime) + "] Current time: ["
                + timeFormat.format(new Date())
                + "], with exception message [" + exceptionMessage + "]");
    }
}
