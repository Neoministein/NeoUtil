package com.neo.util.common.api.func;

/**
 * Add basic runnable for lambdas which throw a checked exception
 *
 * @param <E> checked exception
 */
@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {

    /**
     * Runs the provided functional code
     *
     * @throws E might throw this type of error
     */
    void run() throws E;
}
