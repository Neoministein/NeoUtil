package com.neo.util.common.impl.annotation;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This class is a provides basic Annotation Processor utilities
 */
public class ProcessorUtils {

    private ProcessorUtils(){}

    /**
     * Checks if the {@link TypeElement} implements the provided interface
     *
     * @param typeElement to check
     * @param interfaceClass to check against
     *
     * @throws IllegalStateException if the {@link TypeElement} doesn't impl the provided interface
     */
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

    /**
     * Tries to provide the field value of the annotation on the given class.
     *
     * @param element to check
     * @param annotation of the class
     * @param fieldName of the annotation
     * @return the field value of the annotation
     */
    public static <T> T getAnnotationValue(Element element,
            Class<?> annotation, String fieldName) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors())  {
            if (annotation.getName().equals(annotationMirror.getAnnotationType().toString())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: map.entrySet()) {
                    if (fieldName.equals(entry.getKey().getSimpleName().toString())) {
                        return (T) entry.getValue().getValue();
                    }
                }
            }
        }
        throw new IllegalStateException(
                "Processor configuration failure " + annotation.getName() + " does not contain field name " + fieldName);
    }

    public static List<TypeElement> getTypedElementsAnnotatedWith(RoundEnvironment roundEnv, Elements elements,
            Class<? extends Annotation> annotation, Collection<ElementKind> allowedKinds) {
        List<TypeElement> typeElementList = getSrcTypedElementsAnnotatedWith(roundEnv, annotation, allowedKinds);
        typeElementList.addAll(getDependencyTypedElementsAnnotatedWith(elements, annotation, allowedKinds));
        return typeElementList;
    }


    /**
     * Returns a list of the classes from src which have the annotation.
     *
     * @param roundEnv current env
     * @param annotation the annotation to find
     * @param allowedKinds what is allowed
     * @return a list of TypedElements which have the annotation
     */
    public static List<TypeElement> getSrcTypedElementsAnnotatedWith(RoundEnvironment roundEnv,
            Class<? extends Annotation> annotation, Collection<ElementKind> allowedKinds) {
        List<TypeElement> classList = new ArrayList<>();
        Set<? extends Element> queueConsumerElements = roundEnv.getElementsAnnotatedWith(annotation);
        for (Element element: queueConsumerElements) {
            if (allowedKinds.contains(element.getKind())) {
                classList.add((TypeElement) element);
            }
        }
        return classList;
    }

    /**
     * Returns a list of the classes from dependencies which have the annotation.
     *
     * @param elements current env
     * @param annotation the annotation to find
     * @param allowedKinds what is allowed
     *
     * @return a list of TypedElements which have the annotation
     */
    public static List<TypeElement> getDependencyTypedElementsAnnotatedWith(Elements elements,
            Class<? extends Annotation> annotation, Collection<ElementKind> allowedKinds) {
        List<TypeElement> classList = new ArrayList<>();

        //Get typed element from class
        for (Class<?> clazz: ReflectionUtils.getClassesByAnnotation(annotation)) {
            TypeElement typeElement = elements.getTypeElement(clazz.getName());
            if (typeElement != null && allowedKinds.contains(typeElement.getKind())) {
                classList.add(typeElement);
            }
        }
        return classList;
    }
}
