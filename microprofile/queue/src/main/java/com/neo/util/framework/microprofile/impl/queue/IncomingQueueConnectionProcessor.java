package com.neo.util.framework.microprofile.impl.queue;

import com.google.auto.service.AutoService;
import com.neo.util.common.impl.annotation.AnnotationProcessorUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.OutgoingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.squareup.javapoet.*;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.*;

@SupportedAnnotationTypes("com.neo.util.framework.api.queue.IncomingQueueConnection")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class IncomingQueueConnectionProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingQueueConnectionProcessor.class);

    protected static final String PACKAGE_LOCATION = "com.neo.util.framework.microprofile.reactive.messaging";

    protected static final String BASIC_ANNOTATION_FIELD_NAME = "value";

    protected static final String QUEUE_PREFIX = "from-";

    protected Elements elements;
    protected Filer filer;
    protected Map<String, String> existingIncomingAnnotation = new HashMap<>();
    protected Map<String, String> generatedClasses = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elements = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> queueConsumerElements = getDependencyClasses();
        queueConsumerElements.addAll((getSourceClasses(roundEnv)));
        if (queueConsumerElements.isEmpty()) {
            return false;
        }
        LOGGER.debug("Generating associated files for {} annotation", IncomingQueueConnection.class.getName());

        Set<? extends Element> incomingElements = roundEnv.getElementsAnnotatedWith(Incoming.class);
        for (Element element: incomingElements) {
            Parameterizable parameterizable = (Parameterizable) element;
            existingIncomingAnnotation.put((String) AnnotationProcessorUtils.getAnnotationValue(
                    element.getAnnotationMirrors(), Incoming.class, BASIC_ANNOTATION_FIELD_NAME),
                    parameterizable.getEnclosingElement().getSimpleName().toString());
        }
        LOGGER.debug("Existing queues {}", existingIncomingAnnotation);

        for (TypeElement typeElement: queueConsumerElements) {
            AnnotationProcessorUtils.checkRequiredInterface(typeElement, QueueListener.class);
            String queueName = (String) AnnotationProcessorUtils.getAnnotationValue(typeElement.getAnnotationMirrors(), IncomingQueueConnection.class,
                    BASIC_ANNOTATION_FIELD_NAME);

            String existingQueueAnnotationClass = existingIncomingAnnotation.get(QUEUE_PREFIX + queueName);
            if (existingQueueAnnotationClass != null) {
                LOGGER.debug("Skipping incoming class generation for queue {}. It is already in use in {}", queueName, existingQueueAnnotationClass);
                break;
            }

            String alreadyGeneratedClass = generatedClasses.get(queueName);
            if (alreadyGeneratedClass != null) {
                LOGGER.debug("Skipping incoming class generation for queue {}. It has already been generated by {}", queueName, alreadyGeneratedClass);
                break;
            }

            createConsumeClass(queueName, typeElement);
            generatedClasses.put(queueName, typeElement.getSimpleName().toString());

        }
        return false;
    }

    protected List<TypeElement> getSourceClasses(RoundEnvironment roundEnv) {
        List<TypeElement> classList = new ArrayList<>();
        Set<? extends Element> queueConsumerElements = roundEnv.getElementsAnnotatedWith(IncomingQueueConnection.class);
        for (Element element: queueConsumerElements) {
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalStateException(OutgoingQueueConnection.class.getName() + " must annotate a Class");
            }
            classList.add((TypeElement) element);
        }
        return classList;
    }

    protected List<TypeElement> getDependencyClasses() {
        List<TypeElement> classList = new ArrayList<>();
        Reflections reflections = new Reflections("com.neo");
        Set<Class<?>> clazzSet = reflections.get(
                Scanners.SubTypes.of(Scanners.TypesAnnotated.with(IncomingQueueConnection.class)).asClass());
        for (Class<?> clazz: clazzSet) {
            String clazzName = clazz.getName();
            classList.add(elements.getTypeElement(clazzName));
        }
        return classList;
    }

    protected void createConsumeClass(String queueName, TypeElement typeElement) {
        try {
            FieldSpec logger = FieldSpec.builder(Logger.class, "LOGGER")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.getLogger(" + typeElement.getSimpleName().toString() + "Caller.class)",LoggerFactory.class)
                    .build();
            FieldSpec queueConsumer = FieldSpec.builder(TypeName.get(typeElement.asType()), "queueConsumer")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Inject.class)
                    .build();
            MethodSpec consumeMethodBuilder = MethodSpec.methodBuilder("consumeQueue")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(Incoming.class)
                            .addMember(BASIC_ANNOTATION_FIELD_NAME,"$S" , QUEUE_PREFIX + queueName).build())
                    .addParameter(String.class, "msg")
                    .beginControlFlow("try")
                    .addStatement("queueConsumer.onMessage($T.fromJson(msg, $T.class))", JsonUtil.class, QueueMessage.class)
                    .nextControlFlow("catch($T ex)", Exception.class)
                    .addStatement("LOGGER.error($S, ex.getMessage())","Unexpected error occurred while processing a queue [{}], action won't be retried.")
                    .endControlFlow()
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "Caller")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ApplicationScoped.class)
                    .addMethod(consumeMethodBuilder)
                    .addField(queueConsumer)
                    .addField(logger)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_LOCATION, callerClass).build();

            LOGGER.debug("Generating src file {}Caller", typeElement.getSimpleName());
            javaFile.writeTo(filer);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to generate src file for " + typeElement.getSimpleName().toString(), ex);
        }
    }

}
