package com.neo.util.framework.rest.build;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.rest.api.parser.InboundDto;
import com.neo.util.framework.rest.impl.parser.AbstractDtoReader;
import com.squareup.javapoet.*;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.Set;

/**
 * Generates a specific {@link MessageBodyReader} for a {@link InboundDto} based on a {@link AbstractDtoReader}
 */
public class InboundDtoParserBuildStep implements BuildStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundDtoParserBuildStep.class);

    public static final String INBOUND_DTO_SCHEMA_LOCATION = "inbound/";

    public static final String PACKAGE_LOCATION = "com.neo.util.framework.rest.impl.parsing";

    protected static final String BASIC_ANNOTATION_FIELD_NAME = "value";

    @Override
    public void execute(BuildContext context) {
        Set<Class<?>> inboundDtoSet = ReflectionUtils.getClassesByAnnotation(InboundDto.class, context.srcLoader());

        if (inboundDtoSet.isEmpty()) {
            return;
        }

        LOGGER.debug("Generating associated files for {} annotation", InboundDto.class.getName());
        for (Class<?> inboundDto: inboundDtoSet) {
            createClass(context, inboundDto);
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.LIBRARY_BEFORE;
    }

    protected void createClass(BuildContext context, Class<?> inboundDto) {
        String className = inboundDto.getSimpleName() + "Parser";
        String schemaLocation = INBOUND_DTO_SCHEMA_LOCATION + inboundDto.getSimpleName() + ".json";
        try {
            FieldSpec logger = FieldSpec.builder(Logger.class, "LOGGER")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.getLogger(" + className + ".class)",LoggerFactory.class)
                    .build();

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("super($T.class)", ClassName.get(inboundDto))
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
                    .superclass(ParameterizedTypeName.get(ClassName.get(AbstractDtoReader.class), ClassName.get(inboundDto)))
                    .addMethod(getSchemaLocation)
                    .addField(logger)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_LOCATION, callerClass).build();
            javaFile.writeTo(new File(context.sourceOutPutDirectory()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to generate src file for " + inboundDto.getSimpleName(), ex);
        }
    }
}
