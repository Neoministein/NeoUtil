package com.neo.util.framework.rest.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;

import java.lang.annotation.Annotation;
import java.util.Optional;

@ApplicationScoped
public class RestUtils {

    @Context
    protected ResourceInfo resourceInfo;

    public <A extends Annotation> Optional<A> getAnnotation(Class<A> annoationClass) {
        A annotation = resourceInfo.getResourceMethod().getAnnotation(annoationClass);
        if (annotation == null) {
            annotation = resourceInfo.getResourceClass().getAnnotation(annoationClass);
        }
        return Optional.ofNullable(annotation);
    }
}
