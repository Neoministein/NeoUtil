package com.neo.util.framework.microprofile.impl.queue;

import com.google.auto.service.AutoService;
import com.neo.util.common.impl.annotation.AnnotationProcessorUtils;
import com.neo.util.framework.api.queue.OutgoingQueueConnection;
import com.neo.util.framework.api.queue.QueueProducer;
import com.squareup.javapoet.*;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.enterprise.context.ApplicationScoped;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.concurrent.SubmissionPublisher;

@SupportedAnnotationTypes("com.neo.util.framework.api.queue.OutgoingQueueConnection")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class OutgoingQueueConnectionProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingQueueConnectionProcessor.class);

    protected static final String QUEUE_NAME = "value";

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> queueConsumerElements = roundEnv.getElementsAnnotatedWith(OutgoingQueueConnection.class);
        if (!queueConsumerElements.isEmpty()) {
            LOGGER.debug("Generating src files for {}", OutgoingQueueConnection.class.getName());
        }
        for (Element element: queueConsumerElements) {
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalStateException("Annotation @OutgoingQueueConnection must annotate a Class");
            }
            String queueName = (String) AnnotationProcessorUtils.getAnnotationValue(element.getAnnotationMirrors(), OutgoingQueueConnection.class, QUEUE_NAME);
            createConsumeClass(queueName);
        }
        return false;
    }

    protected void createConsumeClass(String queueName) {
        try {

            FieldSpec queueEmitter = FieldSpec.builder(ParameterizedTypeName.get(SubmissionPublisher.class, String.class), "emitter")
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .initializer("new $T<>()", SubmissionPublisher.class)
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
                    .returns(ParameterizedTypeName.get(Publisher.class, String.class))
                    .addAnnotation(AnnotationSpec.builder(Outgoing.class)
                            .addMember(QUEUE_NAME, "$S", queueName).build())
                    .addStatement("return $T.fromPublisher($T.toPublisher(emitter)).buildRs()", ReactiveStreams.class, FlowAdapters.class)
                    .build();
            MethodSpec getQueueName = MethodSpec.methodBuilder("getQueueName")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addAnnotation(Override.class)
                    .addStatement("return $S", queueName)
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(parseToClassName(queueName))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ApplicationScoped.class)
                    .addSuperinterface(QueueProducer.class)
                    .addMethod(addToQueue)
                    .addMethod(produceToQueue)
                    .addMethod(getQueueName)
                    .addField(queueEmitter)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.neo.util.framework.microprofile.reactive.messaging", callerClass).build();

            LOGGER.info("Generating src file");
            System.out.println("Writing file TestAnnotation to package " + javaFile.packageName);
            // tried this doesnt work
            if (javaFile.toJavaFileObject().delete()) {
                System.out.println("Deleted previously generated file");
            }
            javaFile.writeTo(filer);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot generate src file");
        }
    }

    protected String parseToClassName(String queueName) {
        String nonNummeric = queueName.replaceAll("[^a-zA-Z]", "");
        return nonNummeric.substring(0, 1).toUpperCase() + nonNummeric.substring(1) + "Producer";
    }

}
