package com.neo.util.framework.rest.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import java.lang.annotation.Annotation;
import java.util.Optional;

@ApplicationScoped
public class JaxResourceUtils {

    @Context
    protected ResourceInfo resourceInfo;

    @Context
    protected HttpHeaders headers;

    public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass) {
        A annotation = resourceInfo.getResourceMethod().getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = resourceInfo.getResourceClass().getAnnotation(annotationClass);
        }
        return Optional.ofNullable(annotation);
    }


    public MediaType getCurrentMediaType() {
        Optional<Produces> produces = getAnnotation(Produces.class);
        return MediaType.valueOf(produces.get().value()[0]);
    }
}
