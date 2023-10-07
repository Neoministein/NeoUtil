package com.neo.util.common.api.func;

/**
 * Add basic Consumer for lambdas which throw a checked exception
 *
 * @param <E> checked exception
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws E;
}