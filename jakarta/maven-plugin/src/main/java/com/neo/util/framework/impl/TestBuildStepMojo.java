package com.neo.util.framework.impl;

import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.impl.build.BuildStepExecutor;
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
        defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES,
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
                project.getBuild().getDirectory() + "/generated-test-sources/test-annotations",
                project.getBuild().getTestOutputDirectory(),
                ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                        false, false, project.getArtifacts(), new File(project.getBuild().getTestOutputDirectory()))),
                ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                        true, true, project.getArtifacts(), new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory()))));

        buildStepExecutor.executeBuildSteps(buildContext);
    }
}
