package com.neo.util.framework.api.cache.spi;

import com.neo.util.framework.api.cache.Cache;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Can be added to a field, a constructor parameter or a method parameter to inject a {@link Cache}.
 * It allows programmatic interactions with the cache.
 */
@Qualifier
@Target({ FIELD, METHOD, PARAMETER, TYPE })
@Retention(RUNTIME)
public @interface CacheName {

    /**
     * The name of the cache.
     */
    @Nonbinding
    String value();
}
