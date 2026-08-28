package com.neo.util.framework.api.request;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is a CDI injection qualifier
 * <p>
 * In the case where User Information of the current {@link RequestDetails} is
 * needed to get the {@link UserRequestDetails} if needed
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
public @interface UserRequest {
}
