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

@Mojo(name = "source-BuildStep",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class SourceBuildStepMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceBuildStepMojo.class);

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    protected BuildStepExecutor buildStepExecutor = new BuildStepExecutor();

    @Override
    public void execute() throws MojoExecutionException {
        BuildContext buildContext = new BuildContext(
                project.getBuild().getDirectory() + "/generated-sources/annotations",
                project.getBuild().getOutputDirectory(),
                project.getBuild().getDirectory(),
                ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                        false, false, project.getArtifacts(), new File(project.getBuild().getOutputDirectory()))),
                ClassLoaderUtils.generate(new ClassLoaderUtils.BuildConfig(
                        true, false, project.getArtifacts(), new File(project.getBuild().getOutputDirectory()))));

        try {
            buildStepExecutor.executeBuildSteps(buildContext);
        } catch (Exception ex) {
            throw new MojoExecutionException(ex);
        }
    }
}
