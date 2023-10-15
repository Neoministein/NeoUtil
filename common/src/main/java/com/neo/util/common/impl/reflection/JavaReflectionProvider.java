package com.neo.util.common.impl.reflection;

import com.neo.util.common.api.reflection.ReflectionProvider;
import com.neo.util.common.impl.ThreadUtils;
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
import java.util.List;
import java.util.Set;

/**
 * This class provides basic reflection utilities
 */
public class JavaReflectionProvider implements ReflectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaReflectionProvider.class);

    public static final String DEPENDENCY_REFLECTION_PATTERN = "dependencyReflectionPattern";

    protected ClassLoader classLoader;

    public JavaReflectionProvider(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    public JavaReflectionProvider() {
        this.classLoader = ThreadUtils.classLoader();
    }

    @Override
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(getBasicConfig().setScanners(Scanners.TypesAnnotated));
        return reflections.get(Scanners.TypesAnnotated.with(annotation).asClass(classLoader));
    }


    @Override
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        return new Reflections(getBasicConfig()).getSubTypesOf(type);
    }

    @Override
    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation) {
        for (Annotation elementAnnotation: annotation.getAnnotations()) {
            if (elementAnnotation instanceof Target target) {
                return getAnnotatedElement(annotation, target.value());
            }
        }
        throw new UnsupportedOperationException("Annotation " + annotation + " does not have the Target Annotation");
    }

    @Override
    public <T extends Annotation> List<T> getAnnotationInstance(Class<T> annotationClazz) {
        return getAnnotatedElement(annotationClazz)
                .stream()
                .map(clazz -> clazz.getAnnotation(annotationClazz))
                .toList();
    }

    @Override
    public Set<String> getResources(String regex) {
        return new HashSet<>(getResourceReflection().getResources(regex));
    }

    /**
     * Returns all Annotation instances based on the elementTypes to look through.
     *
     * @param annotation the annotation to find
     * @param elementTypes to look through
     *
     * @return a set of {@link AnnotatedElement}
     */
    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation, ElementType... elementTypes) {
        LOGGER.debug("Searching instances of [{}] on types {}", annotation.getName(), elementTypes);
        Scanners[] scanners = getScanners(elementTypes);
        Reflections reflections = new Reflections(getBasicConfig().setScanners(scanners));
        Set<AnnotatedElement> annotationInstances = new HashSet<>();
        for (Scanners scanner: scanners) {
            annotationInstances.addAll(reflections.get(scanner.with(annotation).as(getClassFromScanner(scanner), classLoader)));
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
    protected Scanners[] getScanners(ElementType... elementTypes) {
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
     * Basic configuration for an instance of {@link Reflections}
     */
    protected ConfigurationBuilder getBasicConfig() {
        return new ConfigurationBuilder()
                .forPackage(System.getProperty(DEPENDENCY_REFLECTION_PATTERN,"com.neo"), classLoader).setClassLoaders(new ClassLoader[]{classLoader});
    }

    /**
     * An instance of {@link Reflections} using resource scanner and {@link #getBasicConfig()}
     */
    protected Reflections getResourceReflection() {
        return new Reflections(getBasicConfig().setScanners(Scanners.Resources));
    }
}
