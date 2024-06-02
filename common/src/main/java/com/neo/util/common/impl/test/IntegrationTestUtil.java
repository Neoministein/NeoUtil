package com.neo.util.common.impl.test;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.common.api.test.WakeupCondition;
import com.neo.util.common.impl.ThreadUtils;
import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility for Integration Tests
 */
public final class IntegrationTestUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private IntegrationTestUtil() {}

    /**
     * This method waits for the {@link WakeupCondition} to be true. <p>
     * If the max number of tries are exceeded, {@link  Assertions#fail()} will be invoked.
     */
    public static void sleepUntil(CheckedRunnable<Exception> wakeUpCondition) {
        sleepUntil(100, 50, wakeUpCondition);
    }

    /**
     * This method waits for the {@link WakeupCondition} to be true. <p>
     * If the max number of tries are exceeded, {@link  Assertions#fail()} will be invoked.
     */
    public static void sleepUntil(int millisToSleep, int tries, CheckedRunnable<Exception> wakeUpCondition) {
        try {
            ThreadUtils.fluentWait(millisToSleep, tries, wakeUpCondition);
        } catch (Exception | AssertionError ex) {
            int totalTime = millisToSleep * tries;
            Assertions.fail("WakeupCondition [" + wakeUpCondition + "] not fulfilled after [" + tries + "] tries, total time ["
                    + TIME_FORMAT.format(Instant.ofEpochMilli(totalTime)) + "] Current time: ["
                    + DATE_FORMAT.format(Instant.now())
                    + "], with exception message [" + ex.getMessage() + "]");
        }
    }
}
