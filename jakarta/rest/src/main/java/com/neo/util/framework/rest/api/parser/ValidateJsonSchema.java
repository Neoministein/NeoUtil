package com.neo.util.framework.rest.api.parser;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used to validate {@link com.fasterxml.jackson.databind.JsonNode} based on the provided scheme.
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ValidateJsonSchema {

    /**
     * The location of the JSON schema in the resources.
     *
     * @return the location
     */
    String value();
}
