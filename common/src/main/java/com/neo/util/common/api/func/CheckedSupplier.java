package com.neo.util.common.api.func;

/**
 * Add basic CheckedSupplier for lambdas which throw a checked exception
 *
 * @param <T> the type of the result of the function
 * @param <E> checked exception
 */
public interface CheckedSupplier<T, E extends Exception> {

    /**
     * Gets a result.
     *
     * @return the function result
     * @throws E might throw this type of error
     */
    T get() throws E;
}
