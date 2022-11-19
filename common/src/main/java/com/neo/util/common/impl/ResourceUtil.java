package com.neo.util.common.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class ResourceUtil {

    private static final ExceptionDetails EX_INVALID_URI = new ExceptionDetails(
            "common/file/invalid-URI", "The URI {0} is invalid.", true);
    private static final ExceptionDetails EX_INVALID_FILE = new ExceptionDetails(
            "common/file/invalid-file", "The file {0} is invalid.", true);
    private static final ExceptionDetails EX_CANNOT_READ_FILE_CONTENT = new ExceptionDetails(
            "common/file/cannot-read", "Cannot read file content at {0}.", true);

    private ResourceUtil(){}

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     *
     * @throws ConfigurationException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) {
        if (fileName == null) {
            throw new ConfigurationException(EX_INVALID_URI, (Object) null);
        }

        try (InputStream is = classLoader().getResourceAsStream(fileName)) {
            if (is == null) throw new ConfigurationException(EX_INVALID_FILE, fileName);
            return StringUtils.toString(is, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new ConfigurationException(ex, EX_CANNOT_READ_FILE_CONTENT, fileName);
        }
    }

    /**
     * Returns an array with the files in the folder
     *
     * @param folderLocation path to the folder
     *
     * @return an array with the files in the folder
     */
    public static File[] getFolderContent(String folderLocation) {
        try {
            List<File> content = new LinkedList<>();
            content.toArray(new File[0]);

            Enumeration<URL> urlEnumeration = classLoader().getResources(folderLocation);
            while (urlEnumeration.hasMoreElements()) {
                content.addAll(Arrays.asList(getFolderContent(urlEnumeration.nextElement())));
            }
            return content.toArray(new File[0]);
        } catch (IOException ex) {
            throw new ConfigurationException(ex, EX_CANNOT_READ_FILE_CONTENT, folderLocation);
        }
    }

    /**
     * Returns an array with the files in the folder
     *
     * @param url path to the folder
     *
     * @return an array with the files in the folder
     */
    public static File[] getFolderContent(URL url) {
        try {
            return new File(url.toURI()).listFiles();
        } catch (URISyntaxException ex) {
            throw new ConfigurationException(ex, EX_CANNOT_READ_FILE_CONTENT, url);
        } catch (IllegalArgumentException ex) {
            return new File[0];
        }
    }

    protected static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
