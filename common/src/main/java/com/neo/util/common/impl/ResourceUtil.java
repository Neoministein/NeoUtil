package com.neo.util.common.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public final class ResourceUtil {

    private static final ExceptionDetails EX_INVALID_URI = new ExceptionDetails(
            "common/file/invalid-URI", "The URI {0} is invalid.");
    private static final ExceptionDetails EX_INVALID_FILE = new ExceptionDetails(
            "common/file/invalid-file", "The file {0} is invalid.");
    private static final ExceptionDetails EX_CANNOT_READ_FILE_CONTENT = new ExceptionDetails(
            "common/file/cannot-read", "Cannot read file content at {0}.");

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

        try (InputStream is = ThreadUtils.classLoader().getResourceAsStream(fileName)) {
            if (is == null) throw new ConfigurationException(EX_INVALID_FILE, fileName);
            return StringUtils.toString(is, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new ConfigurationException(ex, EX_CANNOT_READ_FILE_CONTENT, fileName);
        }
    }

    /**
     * Reads given resource file as a list of lines.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     *
     * @throws ConfigurationException if read fails for any reason
     */
    public static List<String> getResourceFileAsList(String fileName) {
        String[] fileContent = getResourceFileAsString(fileName).split("\\r?\\n");
        if (fileContent.length == 1 && StringUtils.isEmpty(fileContent[0])) {
            return List.of();
        }
        return Arrays.asList(fileContent);
    }

    /**
     * Returns an array with the files in the folder
     * <p/>
     * This method returns an empty array if the folder location is not pointing to a jar file.
     *
     * @param folderLocation path to the folder
     *
     * @return an array with the files in the folder
     */
    public static File[] getFolderContent(String folderLocation) {
        try {
            List<File> content = new LinkedList<>();
            content.toArray(new File[0]);

            Enumeration<URL> urlEnumeration = ThreadUtils.classLoader().getResources(folderLocation);
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
     * <p/>
     * This method returns an empty array if the folder location is not pointing to a jar file.
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
}
