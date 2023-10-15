package com.neo.util.common.impl.reflection;

import com.neo.util.common.api.reflection.ReflectionProvider;
import org.jboss.jandex.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndexReflectionProvider implements ReflectionProvider {

    protected final List<Index> indexes;
    protected final List<Rendex> resources;

    protected final ClassLoader classLoader;


    public IndexReflectionProvider(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.indexes = IndexLoader.loadJandexFiles(classLoader);
        this.resources = IndexLoader.loadRendexFiles(classLoader);
    }

    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        Function<ClassInfo, Class<?>> parseClass = classInfo -> {
            try {
                return parseClass(classInfo);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(ex);
            }
        };
        return getJandexAnnotationInstance(annotation)
                .filter(info -> info.target().kind().equals(AnnotationTarget.Kind.CLASS))
                .map(instance -> instance.target().asClass())
                .map(parseClass)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getResources(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return resources.stream()
                .map(x -> x.getResources(pattern))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation) {
        return getJandexAnnotationInstance(annotation)
                .map(this::asAnnotatedElement)
                .collect(Collectors.toSet());
    }
    @Override
    public <T extends Annotation> List<T> getAnnotationInstance(Class<T> annotationClazz) {
        return getJandexAnnotationInstance(annotationClazz)
                .map(this::asAnnotatedElement)
                .map(x -> x.getAnnotation(annotationClazz))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        Function<ClassInfo, Class<? extends T>> parseClass = classInfo -> {
            try {
                return (Class<? extends T>) parseClass(classInfo);
            } catch (ReflectiveOperationException | ClassCastException ex) {
                throw new IllegalArgumentException(ex);
            }
        };


        if (type.isInterface()) {
            return indexes.stream()
                    .map(index -> index.getAllKnownImplementors(type))
                    .flatMap(Collection::stream)
                    .map(parseClass)
                    .collect(Collectors.toSet());
        } else {
            return Set.of();
        }

    }

    protected Stream<AnnotationInstance> getJandexAnnotationInstance(Class<? extends Annotation> annotation) {
        return indexes.stream().map(index -> index.getAnnotations(annotation)).flatMap(Collection::stream);
    }

    protected <T extends Annotation> T asAnnoationInstance(Class<T> clazz, AnnotationInstance annotationInstance) {
        return asAnnotatedElement(annotationInstance).getAnnotation(clazz);
    }

    protected AnnotatedElement asAnnotatedElement(AnnotationInstance annotationInstance) {
        try {
            if (AnnotationTarget.Kind.CLASS == annotationInstance.target().kind()) {
                return parseClass(annotationInstance.target().asClass());
            } else if (AnnotationTarget.Kind.FIELD == annotationInstance.target().kind()) {
                return parseField(annotationInstance.target().asField());
            } else if (AnnotationTarget.Kind.METHOD == annotationInstance.target().kind()) {
                return parseMethod(annotationInstance.target().asMethod());
            } else if (AnnotationTarget.Kind.METHOD_PARAMETER == annotationInstance.target().kind()) {
                return parseMethodParameter(annotationInstance.target().asMethodParameter());
            }
            throw new IllegalArgumentException("Supplied AnnotationInstance is not yet supported [" + annotationInstance+ "]");
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected Class<?> parseClass(ClassInfo classInfo) throws ReflectiveOperationException {
        return classLoader.loadClass(classInfo.toString());
    }

    protected Field parseField(FieldInfo fieldInfo) throws ReflectiveOperationException {
        Class<?> declaringClass = parseClass(fieldInfo.declaringClass());
        return declaringClass.getDeclaredField(fieldInfo.name());
    }

    protected Method parseMethod(MethodInfo methodInfo) throws ReflectiveOperationException {
        Class<?> declaringClass = parseClass(methodInfo.declaringClass());

        Class<?>[] parameterTypes = new Class[methodInfo.parametersCount()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = classLoader.loadClass(methodInfo.parameterType(i).name().toString());
        }

        return declaringClass.getDeclaredMethod(methodInfo.name(), parameterTypes);
    }

    protected Parameter parseMethodParameter(MethodParameterInfo methodParameterInfo) throws ReflectiveOperationException {
        Method method = parseMethod(methodParameterInfo.method());

        return Arrays.stream(method.getParameters())
                .filter(param -> param.getName().equals(methodParameterInfo.name()))
                .findFirst().orElseThrow();
    }
}
