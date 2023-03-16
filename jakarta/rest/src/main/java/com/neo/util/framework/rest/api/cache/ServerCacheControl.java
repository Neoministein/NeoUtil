package com.neo.util.framework.rest.api.cache;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ServerCacheControl {

    long expireAfter() default EXPIRE_AFTER_DISABLED;

    long EXPIRE_AFTER_DISABLED = -1;
}
