package com.neo.util.common.impl.lazy;

import com.neo.util.common.api.action.Action;
import com.neo.util.common.impl.exception.InternalLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LazyExecutor {

    private static final Logger LOGGER =  LoggerFactory.getLogger(LazyExecutor.class);

    private static final int DEFAULT_STARTING_TIME = 100;

    /**
     * Executes the given action for n amounts of times if the action fails
     *
     * @param actionToExecute the action to Execute
     * @param retries the amount of retries available
     *
     * @throws InternalLazyException if it fails and no more retries are available
     */
    public <T> T execute(Action<T> actionToExecute, int retries, int maxWaitTimeInMilli) {
        return execute(actionToExecute, retries, 0, maxWaitTimeInMilli);
    }

    /**
     * Executes the given action for n amounts of times if the action fails
     *
     * @param actionToExecute the action to Execute
     * @param retries the amount of retries available
     *
     * @throws InternalLazyException if it fails and no more retries are available
     */
    public <T> T execute(Action<T> actionToExecute, int retries) {
        return execute(actionToExecute, retries, 0, DEFAULT_STARTING_TIME);
    }

    protected <T> T execute(Action<T> actionToExecute, int retries, int count, int startingTime) {
        try {
            return actionToExecute.run();
        } catch (InternalLogicException ex) {
            LOGGER.warn("Failed to execute action {} -> retrying {} times", ex.getMessage() ,retries - count);
            if (retries <= count) {
                throw new InternalLazyException("Lazy action cannot be fulfilled request");
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
