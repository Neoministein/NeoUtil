package com.neo.util.tools.unfork;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public abstract class AbstractUnForkOperation extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUnForkOperation.class);

    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(defaultValue = "${localRepository}")
    protected ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteRepositories;

    @Component
    protected ResolutionErrorHandler resolutionErrorHandler;

    @Component
    protected RepositorySystem repositorySystem;

    protected void extract(String sourceDirectory, String resourceDirectory, String sourcesClassifier,
            String resourcesClassifier) throws MojoExecutionException {
        Artifact originalArtifact = resolveOriginalArtifact(sourcesClassifier);
        LOGGER.info("Found original artifact [{}]", originalArtifact.getFile().getAbsolutePath());

        String generateSourcesDirName = project.getBasedir() + "/target/generated-" + sourcesClassifier
                + sourceDirectory;
        String generateResourcesDirName = project.getBasedir() + "/target/generated-" + resourcesClassifier
                + resourceDirectory;

        String projectSrcJavaDirName = project.getBasedir() + sourceDirectory;
        String projectSrcResourceDirName = project.getBasedir() + resourceDirectory;

        File generateSourcesDir = new File(generateSourcesDirName);
        File generateResourcesDir = new File(generateResourcesDirName);

        // add the generated-sources directory as compile directory
        project.addCompileSourceRoot(generateSourcesDirName);

        // add the generated-resources directory as resource directory
        Resource resource = new Resource();
        resource.setDirectory(generateResourcesDirName);
        project.addResource(resource);

        LOGGER.info("Source directory: [{}] added.", generateSourcesDirName);
        LOGGER.info("Source directory: [{}] added.", generateResourcesDirName);

        // create the directories
        try {
            Files.createDirectories(generateSourcesDir.toPath());
            Files.createDirectories(generateResourcesDir.toPath());
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to necessary directory", ex);
        }


        //Unzip the source code
        new ZipUtils().unzipArchive(originalArtifact.getFile(), generateSourcesDir, relativeFileName -> {
            boolean doesFileExist = new File(projectSrcJavaDirName, relativeFileName).exists();
            return relativeFileName.endsWith(".java") && !doesFileExist;

        });

        //Unzip the resources
        new ZipUtils().unzipArchive(originalArtifact.getFile(), generateResourcesDir, relativeFileName -> {
            boolean doesFileExist = new File(projectSrcResourceDirName, relativeFileName).exists();
            return !relativeFileName.endsWith(".java") && !doesFileExist;
        });

        removeEmptyDirectoryRecursive(generateSourcesDir);
        removeEmptyDirectoryRecursive(generateResourcesDir);
    }

    protected void removeEmptyDirectoryRecursive(File dir) throws MojoExecutionException {
        try {
            if (dir.isDirectory()) {
                for (File child : dir.listFiles()) {
                    removeEmptyDirectoryRecursive(child);
                }

                if (dir.listFiles().length == 0) {
                    LOGGER.info("Delete empty folder [{}]", dir);
                    Files.delete(dir.toPath());
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to delete left over files under path " + dir, ex);
        }
    }

    protected Artifact resolveOriginalArtifact(String classifier) throws MojoExecutionException {
        int versionCustomizationSeparator =  project.getVersion().lastIndexOf('-');
        if (versionCustomizationSeparator == -1) {
            throw new MojoExecutionException("The provided maven version does not follow the format originalVersion-customizationName");
        }

        String version = project.getVersion().substring(0, versionCustomizationSeparator);
        return resolveArtifactFromMaven(project.getGroupId(), project.getArtifactId(), version, project.getPackaging(), classifier);

    }

    protected Artifact resolveArtifactFromMaven(String groupId, String artifactId, String version, String type,
            String classifier) throws MojoExecutionException {

        Artifact dummyartifact = repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type,
                classifier);

        ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest()
                .setArtifact(dummyartifact)
                .setLocalRepository(localRepository)
                .setRemoteRepositories(remoteRepositories);

        ArtifactResolutionResult artifactResolutionResult = repositorySystem.resolve(artifactResolutionRequest);

        try {
            resolutionErrorHandler.throwErrors(artifactResolutionRequest, artifactResolutionResult);
        } catch (ArtifactResolutionException ex) {
            throw new MojoExecutionException("Unable to resolve artifact", ex);
        }

        return artifactResolutionResult.getArtifacts().stream().findFirst().orElseThrow(() ->
                new MojoExecutionException("Unable to resolve artifact" + dummyartifact));
    }
}
