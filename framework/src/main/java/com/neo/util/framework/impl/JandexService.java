package com.neo.util.framework.impl;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.annotation.JandexLoader;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JandexService {

    protected final Optional<Index> index;


    public JandexService() {
        this.index = JandexLoader.getIndex();
    }

    public JandexService(Index index) {
        this.index = Optional.ofNullable(index);
    }

    public Optional<Index> getIndex() {
        return index;
    }

    public List<AnnotationInstance> getAnnotationInstance(Class<? extends Annotation> annotation) {
        return index.orElseThrow().getAnnotations(DotName.createSimple(annotation.getName()));
    }


    public Class<?> getClass(AnnotationInstance annotationInstance) {
        return getClass(annotationInstance, ThreadUtils.classLoader());
    }

    public Class<?> getClass(AnnotationInstance annotationInstance, ClassLoader classLoader) {
        if (AnnotationTarget.Kind.CLASS == annotationInstance.target().kind()) {
            try {
                return classLoader.loadClass(annotationInstance.target().asClass().toString());
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        throw new IllegalArgumentException("Supplied AnnotationInstance is not of type Class [" + annotationInstance+ "]");
    }
}
