package com.neo.util.framework.impl.json;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.impl.ClassLoaderUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

@Mojo(name = "generate-jsonSchemaIndex",
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class JsonSchemaIndexMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaIndexMojo.class);

    protected ClassLoader srcLoader;
    protected ClassLoader testLoader;

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        LOGGER.info("Generating index files for all json schemas located inside {}", FrameworkConstants.JSON_SCHEMA_LOCATION);
        createClassLoaders();

        LOGGER.debug("Generating index for src...");
        Set<String> srcFiles = ReflectionUtils.getResources(
                FrameworkConstants.JSON_SCHEMA_LOCATION, ReflectionUtils.JSON_FILE_ENDING, srcLoader);
        generateIndexFile(srcFiles, project.getBuild().getOutputDirectory());


        LOGGER.debug("Generating index for test...");
        Set<String> testFiles = ReflectionUtils.getResources(
                FrameworkConstants.JSON_SCHEMA_LOCATION, ReflectionUtils.JSON_FILE_ENDING, testLoader);
        generateIndexFile(testFiles , project.getBuild().getTestOutputDirectory());
    }

    protected void generateIndexFile(Set<String> files, String outputDir) {
        String filePath = outputDir.concat("/").concat(FrameworkConstants.JSON_SCHEMA_INDEX);
        try {
            File indexFile = new File(filePath);
            indexFile.getParentFile().mkdirs();
            Files.writeString(indexFile.toPath(), String.join("\n", files));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.info("Generated index file at {}", filePath);
    }

    public void createClassLoaders() throws MojoExecutionException {
        srcLoader = ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                true, false, project.getArtifacts(), new File(project.getBuild().getOutputDirectory())));
        testLoader = ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                true, true,project.getArtifacts(), new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory()))
        );
    }
}
