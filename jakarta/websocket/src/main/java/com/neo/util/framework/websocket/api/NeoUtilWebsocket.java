package com.neo.util.framework.websocket.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Websocket Logic
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NeoUtilWebsocket {

    boolean monitored() default false;

    boolean secured() default false;

    String[] roles() default {};
}
