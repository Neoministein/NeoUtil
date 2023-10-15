package com.neo.util.framework.microprofile.reactive.messaging.build;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueProducer;
import com.neo.util.framework.microprofile.reactive.messaging.api.ReactiveMessageTransformer;
import com.neo.util.framework.microprofile.reactive.messaging.api.ReactiveMessageTransformerService;
import com.squareup.javapoet.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SubmissionPublisher;

/**
 * Generates a microprofile specific impl for a {@link OutgoingQueue}
 */
public class OutgoingQueueConnectionProcessor implements BuildStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingQueueConnectionProcessor.class);

    protected static final String PACKAGE_LOCATION = "com.neo.util.framework.microprofile.reactive.messaging";

    protected static final String BASIC_ANNOTATION_FIELD_NAME = "value";
    public static final String QUEUE_PREFIX = "to-";

    protected Map<String, Class<?>> existingOutgoingAnnotation = new HashMap<>();
    protected Map<String, Class<?>> generatedClasses = new HashMap<>();

    @Override
    public void execute(BuildContext context) {
        Set<AnnotatedElement> queueProducerClasses = context.fullReflection().getAnnotatedElement(OutgoingQueue.class);
        if (queueProducerClasses.isEmpty()) {
            return;
        }

        LOGGER.debug("Generating associated files for {} annotation", OutgoingQueue.class.getName());
        Set<AnnotatedElement> incomingClasses = context.fullReflection().getAnnotatedElement(Outgoing.class);
        for (AnnotatedElement element: incomingClasses) {
            Method method = (Method) element;
            existingOutgoingAnnotation.put(method.getAnnotation(Outgoing.class).value(),
                    method.getDeclaringClass());
        }
        LOGGER.debug("Existing queues {}", existingOutgoingAnnotation);

        for (AnnotatedElement element: queueProducerClasses) {
            Class<?> executionMethod = (Class<?>) element;
            String queueName = element.getAnnotation(OutgoingQueue.class).value();

            Class<?> existingQueueAnnotationClass = existingOutgoingAnnotation.get(QUEUE_PREFIX + queueName);
            if (existingQueueAnnotationClass != null) {
                LOGGER.debug("Skipping outgoing class generation for queue {}. It is already in use in {}", queueName, existingQueueAnnotationClass.getSimpleName());
                break;
            }

            Class<?> alreadyGeneratedClass = generatedClasses.get(queueName);
            if (alreadyGeneratedClass != null) {
                LOGGER.debug("Skipping outgoing class generation for queue {}. It has already been generated by {}", queueName, alreadyGeneratedClass);
                break;
            }

            createConsumeClass(queueName, executionMethod, context);
            generatedClasses.put(queueName, executionMethod);
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.LIBRARY_BEFORE;
    }

    protected void createConsumeClass(String queueName, Class<?> executionMethod, BuildContext context) {
        try {
            String className = parseToClassName(queueName);
            FieldSpec queueEmitter = FieldSpec.builder(ParameterizedTypeName.get(SubmissionPublisher.class, String.class), "emitter")
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .initializer("new $T<>()", SubmissionPublisher.class)
                    .build();
            FieldSpec messageTransformer = FieldSpec.builder(TypeName.get(ReactiveMessageTransformer.class), "reactiveMessageTransformer")
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .build();
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Inject.class)
                    .addParameter(ReactiveMessageTransformerService.class, "messageTransformerService")
                    .addStatement("this.$N = $N.getTransformer($S)", "reactiveMessageTransformer", "messageTransformerService", queueName)
                    .build();

            MethodSpec addToQueue = MethodSpec.methodBuilder("addToQueue")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addAnnotation(Override.class)
                    .addParameter(String.class, "msg")
                    .addStatement("emitter.submit(msg)")
                    .build();
            MethodSpec produceToQueue = MethodSpec.methodBuilder("addToQueue")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(PublisherBuilder.class, Object.class))
                    .addAnnotation(AnnotationSpec.builder(Outgoing.class)
                            .addMember(BASIC_ANNOTATION_FIELD_NAME, "$S", QUEUE_PREFIX + queueName).build())
                    .addStatement("return $T.fromPublisher($T.toPublisher(emitter)).map(reactiveMessageTransformer.getMessageTransformer($S))", ReactiveStreams.class, FlowAdapters.class, queueName)
                    .build();
            MethodSpec getQueueName = MethodSpec.methodBuilder("getQueueName")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addAnnotation(Override.class)
                    .addStatement("return $S", queueName)
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ApplicationScoped.class)
                    .addSuperinterface(QueueProducer.class)
                    .addMethod(constructor)
                    .addMethod(addToQueue)
                    .addMethod(produceToQueue)
                    .addMethod(getQueueName)
                    .addField(queueEmitter)
                    .addField(messageTransformer)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_LOCATION, callerClass).build();
            javaFile.writeTo(new File(context.sourceOutPutDirectory()));
            LOGGER.debug("Generating src file {}", className);

        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to generate src file for " + executionMethod.getName(), ex);
        }
    }

    protected String parseToClassName(String queueName) {
        String nonNumeric = queueName.replaceAll("[^a-zA-Z]", "");
        return nonNumeric.substring(0, 1).toUpperCase() + nonNumeric.substring(1) + "Producer";
    }
}
