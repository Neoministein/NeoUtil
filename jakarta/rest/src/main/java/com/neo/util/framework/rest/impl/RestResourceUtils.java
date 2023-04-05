package com.neo.util.framework.rest.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RestResourceUtils {

    @Context
    protected ResourceInfo resourceInfo;

    public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass) {
        A annotation = resourceInfo.getResourceMethod().getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = resourceInfo.getResourceClass().getAnnotation(annotationClass);
        }
        return Optional.ofNullable(annotation);
    }


    public List<String> getValidQueryParamets() {
        List<String> validParameters = new ArrayList<>();


        for (Parameter parameter: resourceInfo.getResourceMethod().getParameters()) {
            QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
            if (queryParam != null) {
                validParameters.add(queryParam.value());
            }
        }
        return validParameters;
    }
}
