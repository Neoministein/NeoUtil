package com.neo.util.common.api.reflection;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Set;

/**
 * The basic necessary functionality for reflective lookup
 */
public interface ReflectionProvider {

    /**
     * Returns a set of classes which are subtypes of the provided class
     *
     * @param type the type to check for subtypes
     *
     * @return a set of classes which are subtypes of the provided class
     *
     * @param <T> the type
     */
    <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type);

    /**
     * Returns all Annotation instances based on the {@link Target} annotation to look through.
     *
     * @param annotation the annotation to find
     *
     * @return a set of {@link AnnotatedElement}
     */
    Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation);

    /**
     * Returns a List of the instances of the annotation.
     *
     * @param annotationClazz the annotation to find
     * @return a List of instances of the annotation
     * @param <T> the annotation type
     */
    <T extends Annotation> List<T> getAnnotationInstance(Class<T> annotationClazz);

    /**
     * Returns a set of the classes which have the annotation.
     *
     * @param annotation the annotation to find
     *
     * @return a set of classes which have the annotation
     */
    Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation);

    /**
     * Returns a set of resources which meet the given criteria
     *
     * @param regex a regex pattern to filter the result by
     * @return a set of resources which meet the given criteria
     */
    Set<String> getResources(String regex);
}