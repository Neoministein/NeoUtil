package com.neo.util.common.impl.retry;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class RetryExecutor {

    private static final Logger LOGGER =  LoggerFactory.getLogger(RetryExecutor.class);

    private static final int DEFAULT_STARTING_TIME = 100;

    protected static final ExceptionDetails EX_RETRY = new ExceptionDetails(
            "common/retry", "Retry action cannot be fulfilled after {0} tries.", true
    );

    /**
     * Executes the given action for n amounts of times if the action fails
     *
     * @param action the action to Execute
     * @param retries the amount of retries available
     *
     * @throws CommonRuntimeException if it fails and no more retries are available
     */
    public <T> T execute(Supplier<T> action, int retries, int maxWaitTimeInMilli) {
        return execute(action, retries, 0, maxWaitTimeInMilli);
    }

    /**
     * Executes the given action for n amounts of times if the action fails
     *
     * @param actionToExecute the action to Execute
     * @param retries the amount of retries available
     *
     * @throws CommonRuntimeException if it fails and no more retries are available
     */
    public <T> T execute(Supplier<T> actionToExecute, int retries) {
        return execute(actionToExecute, retries, 0, DEFAULT_STARTING_TIME);
    }

    protected <T> T execute(Supplier<T> actionToExecute, int retries, int count, int startingTime) {
        try {
            return actionToExecute.get();
        } catch (Exception ex) {
            LOGGER.warn("Failed to execute action {} -> retrying {} times", ex.getMessage(), count);
            if (retries <= count) {
                throw new CommonRuntimeException(EX_RETRY, count);
            }
            wait(count, startingTime);
            execute(actionToExecute, retries, count + 1, startingTime);
        }
        return null;
    }

    protected void wait(int count , int startingTime) {
        long waitFor = (long) (startingTime * (Math.pow(2, count)));

        try {
            Thread.sleep(waitFor);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
