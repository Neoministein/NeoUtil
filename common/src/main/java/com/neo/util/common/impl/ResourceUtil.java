package com.neo.util.common.impl;

import com.neo.util.common.impl.exception.InternalConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

public class ResourceUtil {

    private ResourceUtil(){}

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) throws IOException {
        try (InputStream is = classLoader().getResourceAsStream(fileName)) {
            if (is == null) throw new IOException("Unable to find file: " + fileName);
            return StringUtils.toString(is, Charset.defaultCharset());
        }
    }

    /**
     * Returns an array with the files in the folder
     *
     * @param folderLocation path to the folder
     * @return an array with the files in the folder
     */
    public static File[] getFolderContent(String folderLocation) {
        URL folderURL = classLoader().getResource(folderLocation);
        if (folderURL != null) {
            try {
                return new File(folderURL.toURI()).listFiles();
            } catch (URISyntaxException ex) {
                throw new InternalConfigurationException(ex);
            }
        }
        throw new InternalConfigurationException("Invalid folder path provided [" + folderLocation + "]");

    }

    protected static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
