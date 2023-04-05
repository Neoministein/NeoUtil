package com.neo.util.common.impl.annotation;

import com.neo.util.common.impl.ThreadUtils;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Utility class for Jandex
 */
public class JandexUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JandexUtils.class);

    public static final String DEFAULT_JANDEX_LOCATION = "META-INF/jandex.idx";


    private JandexUtils() {}

    /**
     * Returns optional of Jandex index if file exists based on System property location
     *
     * @return optional of Jandex index if file exists
     */
    public static Optional<Index> getIndex() {
        return getIndex(System.getProperty("jandexFile", DEFAULT_JANDEX_LOCATION));
    }

    /**
     * Returns optional of Jandex index if file exists
     *
     * @return optional of Jandex index if file exists
     */
    public static Optional<Index> getIndex(String indexFileLocation) {
        LOGGER.debug("Trying to load the Jandex index file at [{}]", indexFileLocation);
        URL url = ThreadUtils.classLoader().getResource(indexFileLocation);
        if (url == null) {

            LOGGER.warn("No Jandex index file found");
            return Optional.empty();
        }
        try(FileInputStream input = new FileInputStream(url.getPath())) {
            return Optional.ofNullable(new IndexReader(input).read());
        } catch (IOException ex) {
            LOGGER.error("There was an error parsing the Jandex index file at [{}] {}", indexFileLocation, ex.getMessage());
            return Optional.empty();
        }
    }
}
