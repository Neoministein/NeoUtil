package com.neo.util.framework.impl.rest;

import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.impl.ClassLoaderUtils;
import com.neo.util.framework.rest.api.parser.InboundDto;
import com.neo.util.framework.rest.impl.parser.InboundDtoProcessor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mojo(name = "generate-inboundDto",
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class InboundDtoSchemaMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundDtoSchemaMojo.class);

    protected ClassLoader fullLoader;
    protected ClassLoader srcLoader;
    protected ClassLoader testLoader;

    protected final SchemaGenerator schemaGenerator;

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(property = "includeDependencies", defaultValue = "false")
    private boolean includeDependencies;

    @Parameter(property = "includeTestDependencies", defaultValue = "false")
    private boolean includeTestDependencies;

    public InboundDtoSchemaMojo() {
        JakartaValidationModule jakartaModule = new JakartaValidationModule();
        JacksonModule jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
                .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
                .with(jacksonModule).with(jakartaModule);
        schemaGenerator = new SchemaGenerator(configBuilder.build());
    }

    @Override
    public void execute() throws MojoExecutionException {
        LOGGER.info("Generating schemas for classes annotated with {}", InboundDto.class.getSimpleName());
        createClassLoaders();

        LOGGER.debug("Generating schemas for src...");
        ReflectionUtils.getReflections(srcLoader).get(Scanners.TypesAnnotated.with(InboundDto.class)).forEach(className -> {
            LOGGER.debug("Found class: {}", className);
            saveSchema(className, project.getBuild().getOutputDirectory());
        });

        LOGGER.debug("Generating schemas for test...");
        ReflectionUtils.getReflections(testLoader).get(Scanners.TypesAnnotated.with(InboundDto.class)).forEach(className -> {
            LOGGER.debug("Found class: {}", className);
            saveSchema(className, project.getBuild().getTestOutputDirectory());
        });
    }

    protected void saveSchema(String className, String outputDir) {
        try {
            Class<?> clazz = fullLoader.loadClass(className);
            File schemaFile = new File(generateFileLocation(clazz, outputDir));
            schemaFile.getParentFile().mkdirs();
            Files.writeString(schemaFile.toPath(), schemaGenerator.generateSchema(clazz).toPrettyString());
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        LOGGER.info("Generated schema for {}", className);
    }

    protected String generateFileLocation(Class<?> clazz, String outputDir) {
        StringBuilder file = new StringBuilder(outputDir + "/" + FrameworkConstants.JSON_SCHEMA_LOCATION + "/" + InboundDtoProcessor.INBOUND_DTO_SCHEMA_LOCATION);
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

    protected void createClassLoaders() throws MojoExecutionException {
        fullLoader = ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                true, true, project.getArtifacts(), new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory())));

        srcLoader = ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                includeDependencies, false, project.getArtifacts(), new File(project.getBuild().getOutputDirectory())));

        testLoader = ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                !includeDependencies, includeTestDependencies, project.getArtifacts(), new File(project.getBuild().getTestOutputDirectory())));
    }
}
