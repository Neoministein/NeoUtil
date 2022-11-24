package com.neo.util.framework.rest.impl.parser;

import com.google.auto.service.AutoService;
import com.neo.util.common.impl.annotation.ProcessorUtils;
import com.neo.util.framework.rest.api.parser.InboundDto;
import com.squareup.javapoet.*;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.neo.util.framework.rest.api.parser.InboundDto")
@AutoService(Processor.class)
public class InboundDtoProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundDtoProcessor.class);

    public static final String INBOUND_DTO_SCHEMA_LOCATION = "inbound/";

    public static final String PACKAGE_LOCATION = "com.neo.util.framework.rest.impl.parsing";

    protected static final String BASIC_ANNOTATION_FIELD_NAME = "value";

    protected Filer filer;
    protected Elements elements;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elements = processingEnv.getElementUtils();
    }

   @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> inboundDtoList = ProcessorUtils.getTypedElementsAnnotatedWith(roundEnv, elements,
                InboundDto.class, List.of(ElementKind.CLASS, ElementKind.RECORD));
        if (inboundDtoList.isEmpty()) {
            return true;
        }

        LOGGER.debug("Generating associated files for {} annotation", InboundDto.class.getName());
        for (TypeElement typeElement: inboundDtoList) {
            createClass(typeElement);
        }
        return true;
    }

    protected void createClass(TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString() + "Parser";
        String schemaLocation = INBOUND_DTO_SCHEMA_LOCATION + typeElement.getSimpleName().toString() + ".json";
        try {
            FieldSpec logger = FieldSpec.builder(Logger.class, "LOGGER")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.getLogger(" + className + ".class)",LoggerFactory.class)
                    .build();

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("super($T.class)", ClassName.get(typeElement))
                    .build();

            MethodSpec getSchemaLocation = MethodSpec.methodBuilder("getSchemaLocation")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addStatement("return $S", schemaLocation)
                    .returns(String.class)
                    .build();

            TypeSpec callerClass = TypeSpec.classBuilder(className)
                    .addMethod(constructor)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Provider.class)
                    .addAnnotation(ApplicationScoped.class)
                    .addAnnotation(AnnotationSpec.builder(Priority.class).addMember(BASIC_ANNOTATION_FIELD_NAME, "$L", Priorities.ENTITY_CODER).build())
                    .superclass(ParameterizedTypeName.get(ClassName.get(AbstractDtoParser.class), ClassName.get(typeElement)))
                    .addMethod(getSchemaLocation)
                    .addField(logger)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_LOCATION, callerClass).build();

            LOGGER.debug("Generating src file {}", className);
            javaFile.writeTo(filer);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to generate src file for " + typeElement.getSimpleName().toString(), ex);
        }
    }
}
