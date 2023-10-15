package com.neo.util.common.impl.reflection;

import com.neo.util.common.api.reflection.IndexResolver;
import com.neo.util.common.impl.reflection.resolver.JandexFileSystemResolver;
import com.neo.util.common.impl.reflection.resolver.JandexJarResolver;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

/**
 * Utility class to load the Jandex Indexes
 */
public class IndexLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexLoader.class);

    public static final String JANDEX_FILE_LOCATION = "META-INF/jandex.idx";
    public static final String RENDEX_FILE_LOCATION = "META-INF/rendex.idx";

    public static final List<IndexResolver> DEFAULT_JANDEX_RESOLVERS = List.of(new JandexJarResolver(), new JandexFileSystemResolver());

    private IndexLoader() {}

    public static List<Rendex> loadRendexFiles(ClassLoader classLoader) {
        return loadRendexFiles(classLoader, DEFAULT_JANDEX_RESOLVERS);
    }

    public static List<Index> loadJandexFiles(ClassLoader classLoader) {
        return loadJandexFiles(classLoader, DEFAULT_JANDEX_RESOLVERS);
    }

    public static List<Rendex> loadRendexFiles(ClassLoader classLoader, List<IndexResolver> indexResolvers) {
        LOGGER.debug("Trying to load the Rendex index files");

        Enumeration<URL> indexFileUrls = findIndexFiles(RENDEX_FILE_LOCATION, classLoader);
        if (!indexFileUrls.hasMoreElements()) {
            LOGGER.warn("No Rendex index file found");
            return List.of();
        }

        List<Rendex> indexes = new ArrayList<>();
        while (indexFileUrls.hasMoreElements()) {
            URL url = indexFileUrls.nextElement();
            LOGGER.trace("Trying to parse Rendex index file at [{}]", url);

            Optional<Rendex> optIndex = resolveJandexFileByPath(indexResolvers, resolver -> resolver.getRendex(url));
            if (optIndex.isPresent()) {
                indexes.add(optIndex.get());
            } else {
                LOGGER.warn("Cannot parse Rendex index file at [{}]", url);
            }
        }
        LOGGER.debug("Scanned [{}] Rendex index files", indexes.size());
        return Collections.unmodifiableList(indexes);
    }

    /**
     * Returns a list of Jandex index files
     */
    public static List<Index> loadJandexFiles(ClassLoader classLoader, List<IndexResolver> jandexResolvers) {
        LOGGER.debug("Trying to load the Jandex index files");

        Enumeration<URL> indexFileUrls = findIndexFiles(JANDEX_FILE_LOCATION, classLoader);
        if (!indexFileUrls.hasMoreElements()) {
            LOGGER.warn("No Jandex index file found");
            return List.of();
        }

        List<Index> indexes = new ArrayList<>();
        while (indexFileUrls.hasMoreElements()) {
            URL url = indexFileUrls.nextElement();
            LOGGER.trace("Trying to parse Jandex index file at [{}]", url);

            Optional<Index> optIndex = resolveJandexFileByPath(jandexResolvers, resolver -> resolver.getJandex(url));
            if (optIndex.isPresent()) {
                indexes.add(optIndex.get());
            } else {
                LOGGER.warn("Cannot parse Jandex index file at [{}]", url);
            }
        }
        LOGGER.debug("Scanned [{}] Jandex index files", indexes.size());
        return Collections.unmodifiableList(indexes);
    }

    private static <T> Optional<T> resolveJandexFileByPath(List<IndexResolver> jandexResolvers, Function<IndexResolver, Optional<T>> func) {
        for (IndexResolver resolver: jandexResolvers) {
            LOGGER.trace("Trying [{}]", resolver.getClass().getSimpleName());

            Optional<T> optIndex = func.apply(resolver);
            if(optIndex.isPresent()) {
                return optIndex;
            }
        }
        return Optional.empty();
    }

    private static Enumeration<URL> findIndexFiles(String indexFile, ClassLoader classLoader) {
        try {
            return classLoader.getResources(indexFile);
        } catch (IOException ex) {
            LOGGER.error("There was an error finding the index file at [{}] [{}]", indexFile, ex.getMessage());
            return Collections.emptyEnumeration();
        }
    }
}
