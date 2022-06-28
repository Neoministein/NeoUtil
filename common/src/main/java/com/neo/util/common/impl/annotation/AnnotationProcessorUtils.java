package com.neo.util.common.impl.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

public class AnnotationProcessorUtils {

    private AnnotationProcessorUtils(){}

    public static void checkRequiredInterface(TypeElement typeElement, Class<?> interfaceClass) {
        boolean doesImplements = false;
        List<? extends TypeMirror> typeMirrors = typeElement.getInterfaces();
        for (TypeMirror typeMirror : typeMirrors) {
            if (interfaceClass.getName().equals(typeMirror.toString())) {
                doesImplements = true;
            }
        }
        if (!doesImplements) {
            throw new IllegalStateException(typeElement.getQualifiedName().toString() + " must implement " + interfaceClass.getName());
        }
    }

    public static Object getAnnotationValue(List<? extends AnnotationMirror> annotationMirrors,
            Class<?> annotation, String fieldName) {
        for (AnnotationMirror annotationMirror : annotationMirrors)  {
            if (annotation.getName().equals(annotationMirror.getAnnotationType().toString())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: map.entrySet()) {
                    if (fieldName.equals(entry.getKey().getSimpleName().toString())) {
                        return entry.getValue().getValue();
                    }
                }
            }
        }
        throw new IllegalStateException(
                "Processor configuration failure " + annotation.getName() + " does not contain field name " + fieldName);
    }
}
