package com.neo.util.framework.websocket;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = InSession.List.class)
public @interface InSession {

    /**
     * The name of the cache.
     */
    //@Nonbinding
    //String cacheName() default "";

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        InSession[] value();
    }

}
