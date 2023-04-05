package com.neo.util.common.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Utility class for Futures
 */
public class FutureUtils {


    private FutureUtils() {}

    /**
     * Awaits the future hand handles the execution
     *
     * @param future to await
     * @param interruptedFunction handles InterruptedException
     * @param exceptionTFunction handles ExecutionException
     * @param <T> type of the future
     *
     * @return the awaited future
     *
     */
    public static <T> T await(Future<T> future, Function<InterruptedException, T> interruptedFunction, Function<ExecutionException, T> exceptionTFunction) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return interruptedFunction.apply(ex);
        } catch (ExecutionException ex) {
            return exceptionTFunction.apply(ex);
        }
    }

    /**
     * Awaits the future hand handles the execution
     *
     * @param future to await
     * @param exceptionTFunction handles all future based exceptions
     * @param <T> type of the future
     *
     * @return the awaited future
     */
    public static <T> T await(Future<T> future, Function<Exception, T> exceptionTFunction) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return exceptionTFunction.apply(ex);
        } catch (ExecutionException ex) {
            return exceptionTFunction.apply(ex);
        }
    }

    /**
     * Awaits the future hand throws the execution
     *
     * @param future to await
     * @param <T> type of the future
     *
     * @return the awaited future
     */
    public static <T> T await(Future<T> future) throws Throwable {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (ex.getCause() != null) {
                throw ex.getCause();
            }
            throw ex;
        } catch (ExecutionException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            }
            throw ex;
        }
    }
}
