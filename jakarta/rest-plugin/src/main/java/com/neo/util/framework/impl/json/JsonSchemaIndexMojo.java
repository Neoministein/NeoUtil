package com.neo.util.framework.impl.json;

import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.impl.ClassLoaderUtils;
import org.apache.maven.artifact.Artifact;
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
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.neo.util.common.impl.annotation.ProcessorUtils.DEPENDENCY_REFLECTION_PATTERN;

@Mojo(name = "generate-jsonSchemaIndex",
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class JsonSchemaIndexMojo extends AbstractMojo {

    protected static final String SCHEMA_LOCATION_PATTERN = FrameworkConstants.JSON_SCHEMA_LOCATION + "/**/*";

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(property = "includeDependencies", defaultValue = "false")
    private boolean generateFromDependencies;

    protected final Log logger = getLog();

    @Override
    public void execute() throws MojoExecutionException {
        generateSrcIndex();
        generateTestIndex();
    }

    protected void generateSrcIndex() throws MojoExecutionException {
        Reflections reflections = getReflection(ClassLoaderUtils.getClassLoader(generateFromDependencies, false, project.getArtifacts(), new File(project.getBuild().getOutputDirectory())));
        generateIndexFile(reflections.getResources(SCHEMA_LOCATION_PATTERN));
    }

    protected void generateTestIndex() throws MojoExecutionException {
        Reflections reflections = getReflection(ClassLoaderUtils.getClassLoader(!generateFromDependencies, true,project.getArtifacts(), new File(project.getBuild().getTestOutputDirectory())));
        generateIndexFile(reflections.getResources(SCHEMA_LOCATION_PATTERN));
    }

    protected Reflections getReflection(ClassLoader classLoader) {
        Configuration config = new ConfigurationBuilder().forPackage(
                System.getProperty(DEPENDENCY_REFLECTION_PATTERN,"com.neo"), classLoader);
        return new Reflections(config);
    }

    protected void generateIndexFile(Set<String> files) {
        try {
            File indexFile = new File(FrameworkConstants.JSON_SCHEMA_INDEX);
            indexFile.getParentFile().mkdirs();
            Files.writeString(indexFile.toPath(), String.join("\n", files));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
