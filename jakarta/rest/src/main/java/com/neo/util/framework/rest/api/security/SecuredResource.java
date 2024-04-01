package com.neo.util.framework.rest.api.security;

import com.neo.util.framework.api.request.UserRequestDetails;
import jakarta.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used to define an endpoint which requires authentication.
 * <p>
 * The implementation should check if {@link UserRequestDetails#getUser()} can be resolved
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface SecuredResource { }