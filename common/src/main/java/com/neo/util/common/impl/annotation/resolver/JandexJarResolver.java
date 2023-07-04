package com.neo.util.common.impl.annotation.resolver;

import com.neo.util.common.api.annoation.JandexResolver;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.UnsupportedVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Resolves and reads a Jandex File inside a Jar archive
 */
public class JandexJarResolver implements JandexResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JandexJarResolver.class);


    public Optional<Index> getIndex(URL url) {
        String archive = getArchiveReference(url);
        try (ZipFile zip = new ZipFile(archive)) {
            //Opens the bean archive and tries to find the index file
            ZipEntry entry = zip.getEntry(JANDEX_INDEX_NAME);
            if (entry != null) {
                LOGGER.trace("Jandex index file found");
                Index index = new IndexReader(zip.getInputStream(entry)).read();

                return Optional.of(index);
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.trace("The Jandex index file is not valid: [{}]", archive);
        } catch (UnsupportedVersion ex) {
            LOGGER.trace("The version of Jandex index file is not supported: [{}]", archive);
        } catch (IOException ex) {
            LOGGER.trace("Cannot get Jandex index file from: [{}], [{}]", archive, ex.getMessage());
        }

        return Optional.empty();
    }

    protected String getArchiveReference(URL url) {
        String ref;
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException ex) {
            LOGGER.trace("Unable to read resource at [{}], [{}]", uri, ex.getMessage());
        }

        if(PROTOCOL_FILE.equals(url.getProtocol())) {
            // Adapt file URL, e.g. "file:///home/weld/META-INF/beans.xml" becomes "/home/weld"
            ref = new File(uri.getSchemeSpecificPart()).getParentFile().getParent();
        } else if(PROTOCOL_JAR.equals(url.getProtocol())) {
            // Attempt to adapt JAR file URL, e.g. "jar:file:/home/duke/duke.jar!/META-INF/beans.xml" becomes "/home/duke/duke.jar"
            // NOTE: Some class loaders may support nested jars, e.g. "jar:file:/home/duke/duke.jar!/lib/foo.jar!/META-INF/beans.xml" becomes
            // "/home/duke/duke.jar!/lib/foo.jar"

            // The decoded part without protocol part, i.e. without "jar:"
            ref = uri.getSchemeSpecificPart();

            if(ref.lastIndexOf(JAR_URL_SEPARATOR) > 0) {
                ref = ref.substring(0, ref.lastIndexOf(JAR_URL_SEPARATOR));
            }
            ref = getBeanArchiveReferenceForJar(ref, url);
        } else {
            LOGGER.trace("Unable to adapt URL: [{}], using its external form instead", url);
            ref = url.toExternalForm();
        }
        LOGGER.trace("Resolved bean archive reference: [{}] for URL: [{}]", ref, url);
        return ref;
    }

    protected String getBeanArchiveReferenceForJar(String path, URL fallback) {
        // jar:file:
        if (path.startsWith(PROTOCOL_FILE_PART)) {
            return path.substring(PROTOCOL_FILE_PART.length());
        }
        if (path.startsWith(PROTOCOL_WAR_PART)) {
            // E.g. for Tomcat with unpackWARs=false return war:file:/webapp.war/WEB-INF/lib/foo.jar
            return path;
        }

        LOGGER.trace("Unable to adapt JAR file URL: [{}], using its external form instead", path);
        return fallback.toExternalForm();
    }
}
