package com.neo.util.common.impl.annotation;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a provides basic reflection utilities
 */
public class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    public static final String DEPENDENCY_REFLECTION_PATTERN = "dependencyReflectionPattern";

    public static final String JSON_FILE_ENDING = ".*\\.json";

    private ReflectionUtils() {}

    /**
     * Returns a list of the classes which have the annotation.
     *
     * @param annotation the annotation to find
     *
     * @return  a set of classes which have the annotation
     */
    public static Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(getBasicConfig(Thread.currentThread().getContextClassLoader()));
        return reflections.get(Scanners.SubTypes.of(Scanners.TypesAnnotated.with(annotation)).asClass());
    }

    /**
     * Provides default classLoader
     *
     * @see #getResources(String, String, ClassLoader)
     */
    public static Set<String> getResources(String path, String filePattern) {
        return getResources(path, filePattern, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Returns a set of resources which meet the given criteria
     *
     * @param path the resource should start at the following path
     * @param filePattern the resource filename should match this regex pattern
     * @param classLoader the classloader to search the resources from
     * @return a set of resources which meet the given criteria
     */
    public static Set<String> getResources(String path, String filePattern, ClassLoader classLoader) {
        return getResourceReflection(classLoader).getResources(filePattern).stream()
                .filter(p -> p.startsWith(path))
                .peek(v -> LOGGER.debug("File found: {}", v))
                .collect(Collectors.toSet());
    }

    /**
     * An instance of {@link Reflections} using resource scanner and {@link #getBasicConfig(ClassLoader)}
     */
    protected static Reflections getResourceReflection(ClassLoader classLoader) {
        return new Reflections(getBasicConfig(classLoader).setScanners(Scanners.Resources));
    }

    /**
     * An instance of {@link Reflections} using {@link #getBasicConfig(ClassLoader)}
     */
    public static Reflections getReflections(ClassLoader classLoader) {
        return new Reflections(getBasicConfig(classLoader));
    }

    /**
     * Basic configuration for an instance of {@link Reflections}
     */
    public static ConfigurationBuilder getBasicConfig(ClassLoader classLoader) {
        return new ConfigurationBuilder().forPackage(System.getProperty(DEPENDENCY_REFLECTION_PATTERN,"com.neo"), classLoader);
    }
}
