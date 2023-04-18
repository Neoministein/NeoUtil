package com.neo.util.framework.microprofile.reactive.messaging.build;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.impl.RequestContextExecutor;
import com.neo.util.framework.impl.connection.QueueRequestDetails;
import com.squareup.javapoet.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.*;
import java.io.File;
import java.util.*;

public class IncomingQueueConnectionProcessor implements BuildStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingQueueConnectionProcessor.class);

    protected static final String PACKAGE_LOCATION = "com.neo.util.framework.microprofile.reactive.messaging";

    protected static final String BASIC_ANNOTATION_FIELD_NAME = "value";

    public static final String QUEUE_PREFIX = "from-";

    protected Map<String, Class<?>> existingIncomingAnnotation = new HashMap<>();
    protected Map<String, Class<?>> generatedClasses = new HashMap<>();

    @Override
    public int priority() {
        return PriorityConstants.LIBRARY_BEFORE;
    }

    @Override
    public void execute(BuildContext context) {
        Set<Class<?>> queueConsumerClasses = ReflectionUtils.getClassesByAnnotation(IncomingQueueConnection.class, context.srcLoader());
        if (queueConsumerClasses.isEmpty()) {
            return;
        }
        LOGGER.debug("Generating associated files for {} annotation", IncomingQueueConnection.class.getName());

        Set<Class<?>> incomingInstances = ReflectionUtils.getClassesByAnnotation(Incoming.class, context.fullLoader());
        for (Class<?> incomingClass: incomingInstances) {
            existingIncomingAnnotation.put(incomingClass.getAnnotation(Incoming.class).value(), incomingClass);
        }
        LOGGER.debug("Existing queues {}", existingIncomingAnnotation);

        for (Class<?> queueConsumer: queueConsumerClasses) {
            if (!queueConsumer.isAssignableFrom(QueueListener.class)) {
                throw new IllegalStateException(queueConsumer.getName() + " must implement " + QueueListener.class.getName());
            }
            String queueName = queueConsumer.getAnnotation(IncomingQueueConnection.class).value();

            Class<?> existingQueueAnnotationClass = existingIncomingAnnotation.get(queueName);
            if (existingQueueAnnotationClass != null) {
                LOGGER.debug("Skipping incoming class generation for queue {}. It is already in use in {}", queueName, existingQueueAnnotationClass.getName());
                break;
            }

            Class<?> alreadyGeneratedClass = generatedClasses.get(queueName);
            if (alreadyGeneratedClass != null) {
                LOGGER.debug("Skipping incoming class generation for queue {}. It has already been generated by {}", queueName, alreadyGeneratedClass.getName());
                break;
            }

            createConsumeClass(queueName, queueConsumer, context);
            generatedClasses.put(queueName, queueConsumer);

        }
    }

    protected void createConsumeClass(String queueName, Class<?> queueConsumerClass, BuildContext context) {
        try {
            FieldSpec logger = FieldSpec.builder(Logger.class, "LOGGER")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.getLogger(" + queueConsumerClass.getSimpleName() + "Caller.class)",LoggerFactory.class)
                    .build();
            FieldSpec queueConsumer = FieldSpec.builder(TypeName.get(queueConsumerClass), "queueConsumer")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Inject.class)
                    .build();
            FieldSpec requestContextExecutor = FieldSpec.builder(RequestContextExecutor.class, "requestContextExecutor")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Inject.class)
                    .build();
            MethodSpec consumeMethodBuilder = MethodSpec.methodBuilder("consumeQueue")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(Incoming.class)
                            .addMember(BASIC_ANNOTATION_FIELD_NAME,"$S" , QUEUE_PREFIX + queueName).build())
                    .addParameter(String.class, "msg")
                    .beginControlFlow("try")
                    .addStatement("final var queueMessage = $T.fromJson(msg, $T.class)", JsonUtil.class, QueueMessage.class)
                    .addStatement("requestContextExecutor.execute(new $T(queueMessage, new $T($S)), () -> queueConsumer.onMessage(queueMessage))",
                            QueueRequestDetails.class, RequestContext.Queue.class, queueName)
                    .nextControlFlow("catch($T ex)", ValidationException.class)
                    .addStatement("LOGGER.error($S, ex.getMessage())","Unable to parse incoming queue message [{}], action won't be retried.")
                    .nextControlFlow("catch($T ex)", Exception.class)
                    .addStatement("LOGGER.error($S, ex.getMessage())","Unexpected error occurred while processing a queue [{}], action won't be retried.")
                    .endControlFlow()
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(queueConsumerClass.getSimpleName() + "Caller")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ApplicationScoped.class)
                    .addMethod(consumeMethodBuilder)
                    .addField(queueConsumer)
                    .addField(logger)
                    .addField(requestContextExecutor)
                    .build();

            LOGGER.debug("Generating src file {}Caller", queueConsumerClass.getSimpleName());
            JavaFile javaFile = JavaFile.builder(PACKAGE_LOCATION, callerClass).build();
            javaFile.writeTo(new File(context.sourceOutPutDirectory()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to generate src file for " + queueConsumerClass.getSimpleName(), ex);
        }
    }
}