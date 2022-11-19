package com.neo.util.framework.rest.api.parser;

import jakarta.ws.rs.ext.MessageBodyReader;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used to generate {@link MessageBodyReader} for the annotated class or record
 */
@Retention(RUNTIME)
@Target({TYPE, RECORD_COMPONENT})
public @interface InboundDto {

}
