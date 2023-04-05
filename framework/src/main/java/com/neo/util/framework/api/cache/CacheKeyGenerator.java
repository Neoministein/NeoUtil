package com.neo.util.framework.api.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;

import java.lang.reflect.Method;

/**
 * Implement this interface to generate a cache key based on the cached method, its parameters or any data available
 * from within the generator. The implementation is injected as a CDI bean if possible or is instantiated using
 * the default constructor otherwise.
 * <p>
 * NOTE: Even if a bean is annotated with {@link ApplicationScoped} the injected bean will function same as a {@link Dependent} bean.
 * This is due to no programmatic lookup being supported in CDI 3.0 and waiting for Helidon version bump to 4.0
 * Therefore {@link RequestScoped} and {@link SessionScoped} are not supported.
 */
public interface CacheKeyGenerator {

    /**
     * Generates a cache key.
     *
     * @param method the cached method
     * @param methodParams the method parameters
     * @return cache key
     */
    Object generate(Method method, Object... methodParams);
}
