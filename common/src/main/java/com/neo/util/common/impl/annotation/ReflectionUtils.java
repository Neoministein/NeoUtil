package com.neo.util.common.impl.annotation;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
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
     * @return a set of classes which have the annotation
     */
    public static Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(getBasicConfig(Thread.currentThread().getContextClassLoader()));
        return reflections.get(Scanners.SubTypes.of(Scanners.TypesAnnotated.with(annotation)).asClass());
    }

    /**
     * Returns all Annotation instances based on the {@link Target} annotation to look through.
     *
     * @param annotation the annotation to find
     * @return a set of instances of the annotation
     *
     * @param <T> classType
     */
    public static <T extends Annotation> Set<T> getAnnotationInstances(Class<T> annotation) {
        for (Annotation elementAnnotation: annotation.getAnnotations()) {
            if (elementAnnotation instanceof Target target) {
                return getAnnotationInstances(annotation, target.value());
            }
        }
        throw new UnsupportedOperationException("Annotation " + annotation + " does not have the Target Annotation");
    }

    /**
     * Returns all Annotation instances based on the elementTypes to look through.
     *
     * @param annotation the annotation to find
     * @param elementTypes to look through
     * @return a set of instances of the annotation
     *
     * @param <T> classType
     */
    public static <T extends Annotation> Set<T> getAnnotationInstances(Class<T> annotation, ElementType... elementTypes) {
        LOGGER.debug("Searching instances of [{}] on types {}", annotation.getName(), elementTypes);
        Scanners[] scanners = getScanners(elementTypes);
        Reflections reflections = new Reflections(getBasicConfig(Thread.currentThread().getContextClassLoader())
                .setScanners(scanners));
        Set<T> annotationInstances = new HashSet<>();
        for (Scanners scanner: scanners) {
            for (AnnotatedElement field: reflections.get(scanner.with(annotation).as(getClassFromScanner(scanner)))) {
                annotationInstances.add(field.getAnnotation(annotation));
            }
        }
        LOGGER.trace("Found [{}] instances of [{}] on types {} {}", annotationInstances.size(), annotation.getName(),
                elementTypes, annotationInstances);
        return annotationInstances;
    }

    /**
     * Parses Element to the equivalent scanner
     *
     * @param elementTypes to parse
     *
     * @return the equivalent scanner
     */
    protected static Scanners[] getScanners(ElementType... elementTypes) {
        Scanners[] scanners = new Scanners[elementTypes.length];
        for (int i = 0; i < elementTypes.length;i++) {
            scanners[i] = switch (elementTypes[i]) {
                case FIELD -> Scanners.FieldsAnnotated;
                case METHOD -> Scanners.MethodsAnnotated;
                case PARAMETER -> Scanners.MethodsParameter;
                case TYPE -> Scanners.TypesAnnotated;
                default -> throw new UnsupportedOperationException("ElementType " + elementTypes[i] + " is not supported");
            };
        }

        return scanners;
    }

    /**
     * Parses the scanner to the equivalent class
     *
     * @param scanners to pars
     * @return the equivalent class
     */
    protected static Class<? extends AnnotatedElement> getClassFromScanner(Scanners scanners) {
        return switch (scanners) {
            case FieldsAnnotated -> Field.class;
            case MethodsAnnotated -> Method.class;
            case MethodsParameter -> Parameter.class;
            case TypesAnnotated -> Class.class;
            default -> throw new UnsupportedOperationException("Scanner " + scanners + " is not supported");
        };
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
