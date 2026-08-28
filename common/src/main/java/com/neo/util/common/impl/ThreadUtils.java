package com.neo.util.common.impl;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.common.api.test.WakeupCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * Utilities for {@link Thread}
 */
public final class ThreadUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);

    private ThreadUtils() {}


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
    public static void fluentWait(int millisToSleep, int maxTries, CheckedRunnable<Exception> wakeUpCondition) throws Exception {
        Exception exception = null;
        AssertionError assertionError = null;

        for (int tryCount = 0; tryCount < maxTries; tryCount++) {
            exception = null;
            assertionError = null;

            try {
                wakeUpCondition.run();
                String totalTime = new SimpleDateFormat("HH:mm:ss.SSS").format(millisToSleep * tryCount);
                LOGGER.info("FluentWait - WakeupCondition [{}] met after [{}] tries, total time [{}]",
                        wakeUpCondition, tryCount, totalTime);
                return;
            } catch (AssertionError ex) {
                assertionError = ex;
                LOGGER.warn("FluentWait - Exception occurred during wake up condition. Current: try [{}] in WakeupCondition [{}]. Exception message was: [{}] [{}]",
                        tryCount, wakeUpCondition, ex.getClass().getSimpleName(), ex.getMessage());
            } catch (Exception ex) {
                exception = ex;
                LOGGER.warn("FluentWait - Exception occurred during wake up condition. Current: try [{}] in WakeupCondition [{}]. Exception message was: [{}] [{}]",
                        tryCount, wakeUpCondition, ex.getClass().getSimpleName(), ex.getMessage());
            }

            ThreadUtils.simpleSleep(millisToSleep);
        }

        if (exception != null) {
            throw exception;
        }
        if (assertionError != null) {
            throw assertionError;
        }

        LOGGER.warn("FluentWait - no more tries left");
        throw new IllegalStateException();
    }

    /**
     * Convenience method for rethrow interrupt
     */
    public static void rethrowInterrupt(CheckedRunnable<InterruptedException> runnable) {
        try {
            runnable.run();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Task interrupted", ex);
        }
    }

    /**
     * A fail fast approach for a list of {@link CompletableFuture} futures. <br>
     * If one fails cancel them all
     */
    public static void asyncFailFast(List<CompletableFuture<Void>> futures) {
        AtomicBoolean cancelled = new AtomicBoolean();

        BiConsumer<Void, Throwable> cancelFunc = (_, t) -> {
            if (t != null && cancelled.compareAndSet(false, true)) {
                futures.forEach(f -> f.cancel(true));
            }
        };

        futures.forEach(f -> f.whenComplete(cancelFunc));

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }


    public static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}