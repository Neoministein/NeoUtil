package com.neo.util.common.impl.annotation;

import com.neo.util.common.api.annoation.JandexResolver;
import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.annotation.resolver.JandexFileSystemResolver;
import com.neo.util.common.impl.annotation.resolver.JandexJarResolver;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Utility class to load the Jandex Indexes
 */
public class JandexLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JandexLoader.class);

    public static final String JANDEX_FILE_LOCATION = "META-INF/jandex.idx";

    private final List<JandexResolver> jandexResolvers;

    public JandexLoader() {
        jandexResolvers = List.of(new JandexJarResolver(),new JandexFileSystemResolver());
    }

    public JandexLoader(List<JandexResolver> resolver) {
        jandexResolvers = resolver;
    }

    /**
     * Returns a list of Jandex index files
     */
    public List<Index> loadIndexFiles() {
        LOGGER.debug("Trying to load the Jandex index files");

        Enumeration<URL> jandexFileUrls = findJandexFiles();
        if (!jandexFileUrls.hasMoreElements()) {
            LOGGER.warn("No Jandex index file found");
            return List.of();
        }

        List<Index> indexes = new ArrayList<>();
        while (jandexFileUrls.hasMoreElements()) {
            URL url = jandexFileUrls.nextElement();
            LOGGER.trace("Trying to parse Jandex index file at [{}]", url);

            Optional<Index> optIndex = resolveJandexFileByPath(url);
            if (optIndex.isPresent()) {
                indexes.add(optIndex.get());
            } else {
                LOGGER.warn("Cannot parse Jandex index file at [{}]", url);
            }
        }
        LOGGER.debug("Scanned [{}] Jandex index files", indexes.size());
        return Collections.unmodifiableList(indexes);
    }

    protected Optional<Index> resolveJandexFileByPath(URL url) {
        for (JandexResolver resolver: jandexResolvers) {
            LOGGER.trace("Trying [{}]", resolver.getClass().getSimpleName());

            Optional<Index> optIndex = resolver.getIndex(url);
            if(optIndex.isPresent()) {
                return optIndex;
            }
        }
        return Optional.empty();
    }

    protected Enumeration<URL> findJandexFiles() {
        try {
            return ThreadUtils.classLoader().getResources(JANDEX_FILE_LOCATION);
        } catch (IOException ex) {
            LOGGER.error("There was an error finding the Jandex index file at [{}] [{}]", JANDEX_FILE_LOCATION, ex.getMessage());
            return Collections.emptyEnumeration();
        }
    }
}
