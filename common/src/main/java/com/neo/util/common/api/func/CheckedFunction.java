package com.neo.util.common.api.func;

/**
 * Add basic Function for lambdas which throw a checked exception
 *
 * @param <E> checked exception
 */
@FunctionalInterface
public interface CheckedFunction<T, R, E extends Exception> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws E;
}