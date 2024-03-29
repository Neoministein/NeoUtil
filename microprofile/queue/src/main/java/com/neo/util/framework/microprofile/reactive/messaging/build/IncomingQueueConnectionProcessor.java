package com.neo.util.framework.microprofile.reactive.messaging.build;

import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.queue.IncomingQueue;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.squareup.javapoet.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * Generates a microprofile specific impl for a {@link IncomingQueue}
 */
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
        Set<Class<?>> queueConsumerClasses = context.fullReflection().getClassesByAnnotation(IncomingQueue.class);
        if (queueConsumerClasses.isEmpty()) {
            return;
        }
        LOGGER.debug("Generating associated files for {} annotation", IncomingQueue.class.getName());

        Set<Class<?>> incomingInstances = context.fullReflection().getClassesByAnnotation(Incoming.class);
        for (Class<?> incomingClass: incomingInstances) {
            existingIncomingAnnotation.put(incomingClass.getAnnotation(Incoming.class).value(), incomingClass);
        }
        LOGGER.debug("Existing queues {}", existingIncomingAnnotation);

        for (Class<?> queueConsumer: queueConsumerClasses) {
            if (!QueueListener.class.isAssignableFrom(queueConsumer)) {
                throw new IllegalStateException(queueConsumer.getName() + " must implement " + QueueListener.class.getName());
            }
            String queueName = queueConsumer.getAnnotation(IncomingQueue.class).value();

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
            FieldSpec instanceIdentification = FieldSpec.builder(InstanceIdentification.class, "instanceIdentification")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Inject.class)
                    .build();
            MethodSpec consumeMethodBuilder = MethodSpec.methodBuilder("consumeQueue")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.get(getAcknowledgment()))
                    .returns(ParameterizedTypeName.get(ClassName.get(CompletionStage.class), ClassName.get(Void.class)))
                    .addAnnotation(AnnotationSpec.builder(Incoming.class)
                            .addMember(BASIC_ANNOTATION_FIELD_NAME,"$S" , QUEUE_PREFIX + queueName).build())
                    .addParameter(ParameterizedTypeName.get(Message.class, String.class),"msg")
                    .addStatement("$T $N", QueueMessage.class, "queueMessage")
                    .beginControlFlow("try")
                    .addStatement("$N = $T.fromJson($N.getPayload(), $T.class)", "queueMessage", JsonUtil.class, "msg", QueueMessage.class)
                    .nextControlFlow("catch($T ex)", ValidationException.class)
                    .addStatement("LOGGER.error($S, ex)","Unable to parse incoming queue message. Action won't be retried.")
                    .addStatement("return msg.ack()")
                    .endControlFlow()

                    .beginControlFlow("try")
                    .addStatement("requestContextExecutor.execute(new $T(instanceIdentification.getInstanceId(), queueMessage, new $T($S)), () -> queueConsumer.onMessage(queueMessage))",
                            QueueRequestDetails.class, QueueRequestDetails.Context.class, queueName)
                    .addStatement("return msg.ack()")
                    .nextControlFlow("catch($T ex)", Exception.class)
                    .addStatement("LOGGER.error($S, ex)","Unexpected error occurred while processing a queue message. Action will be retried based on the retry policy.")
                    .addStatement("return msg.nack(ex)")
                    .endControlFlow()
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(queueConsumerClass.getSimpleName() + "Caller")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ApplicationScoped.class)
                    .addMethod(consumeMethodBuilder)
                    .addField(queueConsumer)
                    .addField(logger)
                    .addField(requestContextExecutor)
                    .addField(instanceIdentification)
                    .build();

            LOGGER.debug("Generating src file {}Caller", queueConsumerClass.getSimpleName());
            JavaFile javaFile = JavaFile.builder(PACKAGE_LOCATION, callerClass).build();
            javaFile.writeTo(new File(context.sourceOutPutDirectory()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to generate src file for " + queueConsumerClass.getSimpleName(), ex);
        }
    }

    public Acknowledgment getAcknowledgment() {
        return new Acknowledgment() {

            @Override
            public Class<Acknowledgment> annotationType() {
                return Acknowledgment.class;
            }

            @Override
            public Strategy value() {
                return Acknowledgment.Strategy.MANUAL;
            }
        };
    }
}
