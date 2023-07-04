package com.neo.util.common.api.annoation;

import org.jboss.jandex.Index;

import java.net.URL;
import java.util.Optional;

/**
 * Handles the reference to a Jandex File
 */
public interface JandexResolver {

    String JANDEX_FILE_NAME = "jandex.idx";
    String JANDEX_INDEX_NAME = "META-INF/" + JANDEX_FILE_NAME;

    String PROTOCOL_FILE = "file";
    String PROTOCOL_WAR = "war";
    String PROTOCOL_JAR = "jar";

    String JAR_URL_SEPARATOR = "!/";
    String CLASS_FILE_EXTENSION = ".class";

    String PROTOCOL_FILE_PART = PROTOCOL_FILE + ":";
    String PROTOCOL_WAR_PART = PROTOCOL_WAR + ":";

    /**
     * Read the index at the associated url.
     *
     * @param url to the jandex file
     *
     * @return a parsed index, if possible.
     */
    Optional<Index> getIndex(URL url);
}
