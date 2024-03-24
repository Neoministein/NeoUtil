package com.neo.util.framework.websocket.api.scope;

import jakarta.enterprise.context.NormalScope;

import java.lang.annotation.*;

@Documented
@NormalScope(passivating = false)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface WebsocketScope {
}
