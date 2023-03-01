package com.neo.util.framework.impl;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClassLoaderUtils {

    private ClassLoaderUtils() {}

    public static ClassLoader getClassLoader(boolean addDependencies, boolean addTestDependencies,
                                             Set<Artifact> artifacts, File... runtimeClasspath) throws MojoExecutionException {
        List<URL> urls = new ArrayList<>(runtimeClasspath.length);
        for ( File file : runtimeClasspath ) {
            try {
                urls.add( file.toURI().toURL() );
            }
            catch (MalformedURLException ex) {
                throw new MojoExecutionException("Unable to resolve classpath entry to URL: " + file.getAbsolutePath(), ex);
            }
        }

        for (Artifact artifact : artifacts) {
            try {
                if (!Artifact.SCOPE_TEST.equals(artifact.getScope())) {
                    if (addDependencies) {
                        urls.add(artifact.getFile().toURI().toURL());
                    }
                } else {
                    if (addTestDependencies) {
                        urls.add(artifact.getFile().toURI().toURL());
                    }
                }
            } catch (MalformedURLException ex) {
                throw new MojoExecutionException( "Unable to resolve URL for dependency " + artifact.getId() + " at " + artifact.getFile().getAbsolutePath(), ex );
            }
        }

        return new URLClassLoader( urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }
}
