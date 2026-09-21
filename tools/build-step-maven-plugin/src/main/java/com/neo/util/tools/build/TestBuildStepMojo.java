package com.neo.util.tools.build;

import com.neo.util.api.build.BuildContext;
import com.neo.util.common.impl.reflection.JavaReflectionProvider;
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

@Mojo(name = "test-BuildStep",
        defaultPhase = LifecyclePhase.TEST_COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class TestBuildStepMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestBuildStepMojo.class);

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    protected BuildStepExecutor buildStepExecutor = new BuildStepExecutor();

    @Override
    public void execute() throws MojoExecutionException {
        BuildContext buildContext = new BuildContext(
                project.getBuild().getTestOutputDirectory(),
                new JavaReflectionProvider(
                ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                        false, false, project.getArtifacts(), new File(project.getBuild().getTestOutputDirectory())))),
                new JavaReflectionProvider(
                ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                        true, true, project.getArtifacts(), new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory())))));

        try {
            buildStepExecutor.executeBuildSteps(buildContext);
        } catch (Exception ex) {
            throw new MojoExecutionException(ex);
        }
    }
}
