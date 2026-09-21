package com.neo.util.jakarta.reflexion;

import com.neo.util.api.event.ApplicationPostReadyEvent;
import com.neo.util.common.api.PriorityConstants;
import com.neo.util.common.api.reflection.ReflectionProvider;
import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class JakartaReflectionProviderWrapper implements ReflectionProvider {

    protected ReflectionProvider reflectionProvider;

    /**
     * Standard Reflexion Provider
     */
    public JakartaReflectionProviderWrapper() {
        reflectionProvider = IndexReflectionProvider.INSTANCE;
    }

    /**
     * For testing purposes
     */
    public JakartaReflectionProviderWrapper(ReflectionProvider reflectionProvider) {
        this.reflectionProvider = reflectionProvider;
    }

    protected ReflectionProvider getProvider() {
        if (reflectionProvider == null) {
            throw new IllegalStateException("This is a programmer mistake, ReflectionProvider should not be called after application startup!");
        }
        return reflectionProvider;
    }

    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        return getProvider().getSubTypesOf(type);
    }

    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation) {
        return getProvider().getAnnotatedElement(annotation);
    }

    public <T extends Annotation> List<T> getAnnotationInstance(Class<T> annotationClazz) {
        return getProvider().getAnnotationInstance(annotationClazz);
    }

    @Override
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        return getProvider().getClassesByAnnotation(annotation);
    }

    @Override
    public Set<String> getResources(String regex) {
        return getProvider().getResources(regex);
    }

    /**
     * Reflection should no longer be needed after startup and the resource should be freed
     */
    public void init(@Observes @Priority(PriorityConstants.PLATFORM_AFTER + 1) ApplicationPostReadyEvent event) {
        reflectionProvider = null; //TODO Add better error handling since now it produces a null pointer
    }
}
