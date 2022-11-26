package com.neo.util.framework.rest.api.parser;

import com.fasterxml.jackson.annotation.JsonView;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines which {@link JsonView} class is used to serialize the outgoing object of a resource method.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface OutboundJsonView {

    Class<?> value();
}
