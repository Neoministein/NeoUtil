package com.neo.util.framework.rest.build;

import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.rest.api.parser.InboundDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Generates a JsonSchema for a {@link InboundDto}
 */
public class InboundDtoSchemaBuildStep implements BuildStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundDtoSchemaBuildStep.class);

    protected final SchemaGenerator schemaGenerator;

    public InboundDtoSchemaBuildStep() {
        JakartaValidationModule jakartaModule = new JakartaValidationModule();
        JacksonModule jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
                .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
                .with(jacksonModule).with(jakartaModule);
        schemaGenerator = new SchemaGenerator(configBuilder.build());
    }

    @Override
    public void execute(BuildContext context) {
        LOGGER.info("Generating schemas for classes annotated with {}", InboundDto.class.getSimpleName());

        LOGGER.debug("Generating schemas for src...");
        context.fullReflection().getClassesByAnnotation(InboundDto.class).forEach(clazz -> {
            LOGGER.debug("Found class: {}", clazz);
            saveSchema(clazz, context);
        });
    }

    @Override
    public int priority() {
        return PriorityConstants.LIBRARY_BEFORE;
    }

    protected void saveSchema(Class<?> clazz, BuildContext buildContext) {
        try {
            File schemaFile = new File(generateFileLocation(clazz, buildContext.resourceOutPutDirectory()));
            Files.deleteIfExists(schemaFile.toPath());

            schemaFile.getParentFile().mkdirs();
            Files.writeString(schemaFile.toPath(), schemaGenerator.generateSchema(clazz).toPrettyString());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        LOGGER.info("Generated schema for {}", clazz);
    }

    protected String generateFileLocation(Class<?> clazz, String outputDir) {
        StringBuilder file = new StringBuilder(outputDir + "/" + FrameworkConstants.JSON_SCHEMA_LOCATION + InboundDtoParserBuildStep.INBOUND_DTO_SCHEMA_LOCATION);
        if (clazz.getSimpleName().indexOf('$') == -1) {
            file.append(clazz.getSimpleName()).append(".json");
        }  else {
            for (String s: clazz.getSimpleName().split("\\$")) {
                file.append(s);
            }
            file.append(".json");
        }
        return file.toString();
    }
}
