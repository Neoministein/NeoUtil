package com.neo.util.tools.rendex;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generate a Rendex index for resources as part of the current project.
 */
@Mojo(name = "rendex", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RendexGoal extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(RendexGoal.class);

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    /**
     * By default, process the resources file in the target folder of the project. If you need to process other sets of
     * files, such as test resources, see the <code>fileSets</code> parameter.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File resourcesDir;

    /**
     * Process the resources found in these file sets, after considering the specified includes and excludes, if any.
     * The format is:
     *
     * <pre>
     * &lt;fileSets&gt;
     *   &lt;fileSet&gt;
     *     &lt;directory&gt;path-or-expression&lt;/directory&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;some/thing/*.good&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;some/thing/*.bad&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/fileSet&gt;
     * &lt;/fileSets&gt;
     * </pre>
     *
     * Instead of the <code>directory</code> element, a <code>dependency</code> element may be used.
     * In that case, if the project has a corresponding dependency, classes in its artifact are processed.
     * The <code>dependency</code> element must specify a <code>groupId</code> and an <code>artifactId</code>
     * and may specify a <code>classifier</code>:
     *
     * <pre>
     * &lt;fileSets&gt;
     *   &lt;fileSet&gt;
     *     &lt;dependency&gt;
     *       &lt;groupId&gt;com.example&lt;/groupId&gt;
     *       &lt;artifactId&gt;my-project&lt;/artifactId&gt;
     *       &lt;classifier&gt;tests&lt;/artifactId&gt;
     *     &lt;/dependency&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;some/thing/*.good&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;some/thing/*.bad&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/fileSet&gt;
     * &lt;/fileSets&gt;
     * </pre>
     *
     * NOTE: Standard globbing expressions are supported in includes/excludes.
     */
    @Parameter
    private List<FileSet> fileSets;

    /**
     * If true, and if a file set rooted in the <code>target/classes</code> directory is not defined explicitly,
     * an implied file set rooted in the <code>target/classes</code> directory will be used.
     */
    @Parameter(defaultValue = "true")
    private boolean processDefaultFileSet;

    /**
     * Print verbose output (debug output without needing to enable -X for the whole build).
     */
    @Parameter(defaultValue = "false")
    private boolean verbose;

    /**
     * The directory in which the index file will be created.
     * Defaults to <code>${project.build.outputDirectory}/META-INF</code>.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF")
    private File indexDir;

    /**
     * The name of the index file. Defaults to <code>jandex.idx</code>.
     */
    @Parameter(defaultValue = "rendex.idx")
    private String indexName;

    /**
     * Persistent index format version to write. Defaults to max supported version.
     */
    @Parameter
    private Integer indexVersion;

    /**
     * Skip execution if set.
     */
    @Parameter(property = "rendex.skip", defaultValue = "false")
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Rendex execution skipped");
            return;
        }
        if ("pom".equals(mavenProject.getPackaging())) {
            getLog().info("Rendex execution skipped - packaging: pom");
        }


        if (fileSets == null) {
            fileSets = new ArrayList<>();
        }

        if (processDefaultFileSet) {
            boolean explicitlyConfigured = false;
            for (FileSet fileset : fileSets) {
                if (fileset.getDirectory() != null && fileset.getDirectory().equals(resourcesDir)) {
                    explicitlyConfigured = true;
                    break;
                }
            }

            if (!explicitlyConfigured) {
                FileSet fs = new FileSet();
                fs.setDirectory(resourcesDir);
                fs.setExcludes(List.of("**/*.class"));
                fileSets.add(fs);
            }
        }

        Set<String> resourceIndex = new HashSet<>();
        for (FileSet fileSet : fileSets) {
            if (fileSet.getDirectory() == null && fileSet.getDependency() == null) {
                throw new MojoExecutionException("File set must specify either directory or dependency");
            }
            if (fileSet.getDirectory() != null && fileSet.getDependency() != null) {
                throw new MojoExecutionException("File set may not specify both directory and dependency");
            }

            if (fileSet.getDirectory() != null) {
                indexDirectory(resourceIndex, fileSet);
            } else if (fileSet.getDependency() != null) {
                indexDependency(resourceIndex, fileSet);
            }
        }

        File indexFile = new File(indexDir, indexName);
        LOGGER.info("Saving Rendex index: [{}]", indexFile);
        try {
            Files.createDirectories(indexFile.toPath().getParent());
            Files.writeString(indexFile.toPath(), resourceIndex.stream().sorted().collect(Collectors.joining(System.getProperty("line.separator"))));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not save index " + indexFile, e);
        }
    }

    private void indexDirectory(Set<String> resourceIndex, FileSet fileSet) {
        File dir = fileSet.getDirectory();
        if (!dir.exists()) {
            LOGGER.warn("Skipping file set, directory does not exist: [{}] ", fileSet.getDirectory());
            return;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(dir);
        String[] files = findFilesToIndex(fileSet, scanner);
        for (String file : files) {
            if (isResourceFile(file)) {
                resourceIndex.add(file);
                if (isVerbose()) {
                    //getLog().info("Indexed " + info.name() + " (" + info.annotationsCount() + " annotations)");
                }
            }
        }
    }

    private void indexDependency(Set<String> resourceIndex, FileSet fileSet) throws MojoExecutionException {
        Dependency dependency = fileSet.getDependency();
        if (dependency.getGroupId() == null) {
            throw new MojoExecutionException("Dependency in file set must specify groupId");
        }
        if (dependency.getArtifactId() == null) {
            throw new MojoExecutionException("Dependency in file set must specify artifactId");
        }

        Artifact artifact = null;
        for (Artifact candidate : mavenProject.getArtifacts()) {
            if (candidate.getGroupId().equals(dependency.getGroupId())
                    && candidate.getArtifactId().equals(dependency.getArtifactId())
                    && (dependency.getClassifier() == null || candidate.getClassifier().equals(dependency.getClassifier()))) {
                artifact = candidate;
                break;
            }
        }
        if (artifact == null) {
            getLog().warn("Skipping file set, artifact not found among this project dependencies: " + dependency);
            return;
        }

        File archive = artifact.getFile();
        if (archive == null) {
            getLog().warn("Skipping file set, artifact file does not exist for dependency: " + dependency);
            return;
        }

        ArchiveScanner scanner = new ArchiveScanner(archive);
        for (String file : findFilesToIndex(fileSet, scanner)) {
            if (isResourceFile(".class")) {
                resourceIndex.add(file);
                if (isVerbose()) {
                    //getLog().info("Indexed " + info.name() + " (" + info.annotationsCount() + " annotations)");
                }
            }
        }
    }

    private String[] findFilesToIndex(FileSet fileSet, Scanner scanner) {
        // order files to get reproducible result
        scanner.setFilenameComparator(String::compareTo);

        if (fileSet.isUseDefaultExcludes()) {
            scanner.addDefaultExcludes();
        }

        List<String> includes = fileSet.getIncludes();
        if (includes != null) {
            scanner.setIncludes(includes.toArray(new String[0]));
        }

        List<String> excludes = fileSet.getExcludes();
        if (excludes != null) {
            scanner.setExcludes(excludes.toArray(new String[0]));
        }

        scanner.scan();
        return scanner.getIncludedFiles();
    }

    private boolean isResourceFile(String file) {
        if (file.endsWith(".class")) {
            return false;
        }
        if (file.endsWith(".jar")) {
            return false;
        }
        if (file.endsWith(".war")) {
            return false;
        }
        return true;
    }

    private boolean isVerbose() {
        return verbose || getLog().isDebugEnabled();
    }
}