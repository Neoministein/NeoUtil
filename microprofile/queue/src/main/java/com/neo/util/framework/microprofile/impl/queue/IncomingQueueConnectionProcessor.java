package com.neo.util.framework.microprofile.impl.queue;

import com.google.auto.service.AutoService;
import com.neo.util.common.impl.annotation.AnnotationProcessorUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.squareup.javapoet.*;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("com.neo.util.framework.api.queue.IncomingQueueConnection")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class IncomingQueueConnectionProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingQueueConnectionProcessor.class);

    protected static final String BASIC_ANNOTATION_FIELD_NAME = "value";

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> queueConsumerElements = roundEnv.getElementsAnnotatedWith(IncomingQueueConnection.class);
        if (!queueConsumerElements.isEmpty()) {
            LOGGER.debug("Generating src files for {}", IncomingQueueConnection.class.getName());
        }
        for (Element element: queueConsumerElements) {
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalStateException("Annotation @IncomingQueueConnection must annotate a Class");
            }
            TypeElement typeElement = (TypeElement) element;
            AnnotationProcessorUtils.checkRequiredInterface(typeElement, QueueListener.class);
            String queueName = (String) AnnotationProcessorUtils.getAnnotationValue(element.getAnnotationMirrors(), IncomingQueueConnection.class,
                    BASIC_ANNOTATION_FIELD_NAME);

            createConsumeClass(queueName, typeElement);
        }
        return false;
    }

    protected void createConsumeClass(String queueName, TypeElement typeElement) {
        try {
            FieldSpec queueConsumer = FieldSpec.builder(TypeName.get(typeElement.asType()), "queueConsumer")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Inject.class)
                    .build();
            MethodSpec consumeMethodBuilder = MethodSpec.methodBuilder("consumeQueue")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(Incoming.class)
                            .addMember(BASIC_ANNOTATION_FIELD_NAME,"$S" ,queueName).build())
                    .addParameter(String.class, "msg")
                    .addStatement("queueConsumer.onMessage($T.fromJson(msg, $T.class))", JsonUtil.class, QueueMessage.class)
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "Caller")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ApplicationScoped.class)
                    .addMethod(consumeMethodBuilder)
                    .addField(queueConsumer)
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

}
