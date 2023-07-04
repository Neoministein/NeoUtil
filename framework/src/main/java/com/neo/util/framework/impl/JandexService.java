package com.neo.util.framework.impl;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.annotation.JandexLoader;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.jandex.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class JandexService {

    protected final List<Index> indexes;


    public JandexService() {
        this.indexes = new JandexLoader().loadIndexFiles();
    }

    public boolean jandexFilesFound() {
        return !indexes.isEmpty();
    }

    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation) {
        return getAnnotationInstance(annotation).stream()
                .map(instance -> asAnnotatedElement(instance,ThreadUtils.classLoader()))
                .collect(Collectors.toSet());
    }

    protected List<AnnotationInstance> getAnnotationInstance(Class<? extends Annotation> annotation) {
        return indexes.stream().map(index -> index.getAnnotations(DotName.createSimple(annotation.getName()))).flatMap(List::stream).toList();
    }

    protected AnnotatedElement asAnnotatedElement(AnnotationInstance annotationInstance, ClassLoader classLoader) {
        try {
            if (AnnotationTarget.Kind.CLASS == annotationInstance.target().kind()) {
                return classLoader.loadClass(annotationInstance.target().asClass().toString());
            } else if (AnnotationTarget.Kind.FIELD == annotationInstance.target().kind()) {
                return parseField(annotationInstance.target().asField(), classLoader);
            } else if (AnnotationTarget.Kind.METHOD == annotationInstance.target().kind()) {
                return parseMethod(annotationInstance.target().asMethod(), classLoader);
            }
            throw new IllegalArgumentException("Supplied AnnotationInstance is not yet supported [" + annotationInstance+ "]");
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected Field parseField(FieldInfo fieldInfo, ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> declaringClass = classLoader.loadClass(fieldInfo.declaringClass().name().toString());
        return declaringClass.getDeclaredField(fieldInfo.name());
    }

    protected Method parseMethod(MethodInfo methodInfo, ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> declaringClass = classLoader.loadClass(methodInfo.declaringClass().name().toString());

        Class<?>[] parameterTypes = new Class[methodInfo.parametersCount()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = classLoader.loadClass(methodInfo.parameterType(i).name().toString());
        }

        return declaringClass.getDeclaredMethod(methodInfo.name(), parameterTypes);
    }
}
