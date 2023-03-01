package com.neo.util.framework.impl.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.impl.ClassLoaderUtils;
import com.neo.util.framework.rest.api.parser.InboundDto;
import com.neo.util.framework.rest.impl.parser.InboundDtoProcessor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static com.neo.util.common.impl.annotation.ProcessorUtils.DEPENDENCY_REFLECTION_PATTERN;

@Mojo(name = "generate-inboundDto",
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class InboundDtoSchemaMojo extends AbstractMojo {


    protected final SchemaGenerator schemaGenerator;

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(property = "includeDependencies", defaultValue = "false")
    private boolean generateFromDependencies;

    @Parameter(property = "includeTestDependencies", defaultValue = "false")
    private boolean generateFromDependenciesForTest;

    protected final Log logger = getLog();

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
        ClassLoader fullClassLoader = ClassLoaderUtils.getClassLoader(true, true, project.getArtifacts() ,new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory()));
        generateForSrc(fullClassLoader);
        generateForTest(fullClassLoader);
    }

    protected void generateForSrc(ClassLoader fullClassLoader) throws MojoExecutionException {
        Reflections reflections = getReflection(ClassLoaderUtils.getClassLoader(generateFromDependencies, false,project.getArtifacts() ,new File(project.getBuild().getOutputDirectory())));
        Set<String> classList = reflections.get(Scanners.TypesAnnotated.with(InboundDto.class));
        for (String clazz: classList) {
            try {
                saveSchema(fullClassLoader.loadClass(clazz), project.getBuild().getOutputDirectory());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void generateForTest(ClassLoader fullClassLoader) throws MojoExecutionException {
        Reflections reflections = getReflection(ClassLoaderUtils.getClassLoader(!generateFromDependencies, generateFromDependenciesForTest, project.getArtifacts() ,new File(project.getBuild().getTestOutputDirectory())));
        Set<String> classList = reflections.get(Scanners.TypesAnnotated.with(InboundDto.class));
        for (String clazz: classList) {
            try {
                saveSchema(fullClassLoader.loadClass(clazz), project.getBuild().getTestOutputDirectory());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Reflections getReflection(ClassLoader classLoader) {
        Configuration config = new ConfigurationBuilder().forPackage(
                System.getProperty(DEPENDENCY_REFLECTION_PATTERN,"com.neo"), classLoader);
        return new Reflections(config);
    }

    protected void saveSchema(Class<?> clazz, String outputDir) {
        JsonNode schema = schemaGenerator.generateSchema(clazz);
        try {
            StringBuilder file = new StringBuilder(outputDir + "/" + FrameworkConstants.JSON_SCHEMA_LOCATION + "/" + InboundDtoProcessor.INBOUND_DTO_SCHEMA_LOCATION);
            if (clazz.getSimpleName().indexOf('$') == -1) {
                file.append(clazz.getSimpleName()).append(".json");
            }  else {
                for (String s: clazz.getSimpleName().split("\\$")) {
                    file.append(s);
                }
                file.append(".json");
            }
            File schemaFile = new File(file.toString());
            schemaFile.getParentFile().mkdirs();
            Files.writeString(schemaFile.toPath(), schema.toPrettyString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
