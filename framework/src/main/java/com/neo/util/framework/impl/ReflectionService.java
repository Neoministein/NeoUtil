package com.neo.util.framework.impl;

import com.neo.util.common.api.reflection.ReflectionProvider;
import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

@ApplicationScoped
public class ReflectionService {

    protected ReflectionProvider reflectionProvider;

    public ReflectionService() {
        reflectionProvider = new IndexReflectionProvider(ThreadUtils.classLoader());
    }

    public ReflectionService(ReflectionProvider reflectionProvider) {
        //For testing purposes
        this.reflectionProvider = reflectionProvider;
    }

    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation) {
        return reflectionProvider.getAnnotatedElement(annotation);
    }

    /**
     * Returns a set of resources which meet the given criteria
     *
     * @param regex a regex pattern to filter the result by
     * @return a set of resources which meet the given criteria
     */
    public Set<String> getResources(String regex) {
        return reflectionProvider.getResources(regex);
    }

    /**
     * Reflection should no longer be needed after startup and the resource should be freed
     */
    public void init(@Observes @Priority(PriorityConstants.PLATFORM_AFTER + 1) ApplicationPostReadyEvent event) {
        reflectionProvider = null;
    }
}
