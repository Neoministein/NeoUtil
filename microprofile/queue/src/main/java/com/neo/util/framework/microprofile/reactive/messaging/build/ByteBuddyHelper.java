package com.neo.util.framework.microprofile.reactive.messaging.build;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.annotation.Annotation;
import java.util.function.Function;

public class ByteBuddyHelper {

    public static AnnotationDescription annotation(Class<? extends Annotation> annotation, Function<AnnotationDescription.Builder, AnnotationDescription.Builder> a) {
        return a.apply(AnnotationDescription.Builder.ofType(annotation)).build();
    }

    public static AnnotationDescription annotation(Class<? extends Annotation> annotation) {
        return AnnotationDescription.Builder.ofType(annotation).build();
    }

    public static TypeDescription.Generic type(Class<?> rawType, java.lang.reflect.Type... parameter) {
        return  TypeDescription.Generic.Builder.parameterizedType(rawType, parameter).build();
    }

}
