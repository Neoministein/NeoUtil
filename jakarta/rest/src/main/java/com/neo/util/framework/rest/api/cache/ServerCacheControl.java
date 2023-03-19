package com.neo.util.framework.rest.api.cache;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Can be added to any Jax Rs resource method or class to cache the response server side
 * <p>
 * - Only occurs if the status code is 200
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ServerCacheControl {

    /**
     * Time in seconds after the cache should no longer be valid
     */
    long expireAfter() default EXPIRE_AFTER_DISABLED;

    long EXPIRE_AFTER_DISABLED = -1;
}
