package com.neo.util.framework.impl;

import com.neo.util.common.impl.json.JsonUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClassLoaderUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderUtils.class);

    private ClassLoaderUtils() {}

    public static ClassLoader generate(BuildConfig config) throws MojoExecutionException {
        LOGGER.debug("Generating custom ClassLoader, \"classLoaderConfig\":{}", config);
        List<URL> urls = new ArrayList<>(config.runtimeClasspath().length);
        for ( File file : config.runtimeClasspath()) {
            try {
                urls.add( file.toURI().toURL() );
            }
            catch (MalformedURLException ex) {
                throw new MojoExecutionException("Unable to resolve classpath entry to URL: " + file.getAbsolutePath(), ex);
            }
        }

        for (Artifact artifact : config.artifacts()) {
            try {
                if (!Artifact.SCOPE_TEST.equals(artifact.getScope())) {
                    if (config.includeDependencies()) {
                        urls.add(artifact.getFile().toURI().toURL());
                    }
                } else {
                    if (config.includeTestDependencies()) {
                        urls.add(artifact.getFile().toURI().toURL());
                    }
                }
            } catch (MalformedURLException ex) {
                throw new MojoExecutionException("Unable to resolve URL for dependency " + artifact.getId() + " at " + artifact.getFile().getAbsolutePath(), ex );
            }
        }

        return new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }

    public record BuildConfig(
            boolean includeDependencies,
            boolean includeTestDependencies,
            Set<Artifact> artifacts,
            File... runtimeClasspath) {

        @Override
        public String toString() {
            return JsonUtil.toJson(this);
        }
    }
}
