package com.neo.util.common.impl.reflection.resolver;

import com.neo.util.common.api.func.CheckedFunction;
import com.neo.util.common.api.reflection.IndexResolver;
import com.neo.util.common.impl.reflection.Rendex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.UnsupportedVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Resolves and reads a Jandex File inside a Jar archive
 */
public class JandexJarResolver implements IndexResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JandexJarResolver.class);

    @Override
    public Optional<Index> getJandex(URL url) {
        return readFile(JANDEX_INDEX_NAME, url, is -> new IndexReader(is).read());
    }

    @Override
    public Optional<Rendex> getRendex(URL url) {
        return readFile(RENDEX_INDEX_NAME, url, is -> new Rendex(is));
    }

    public <T> Optional<T> readFile(String filename, URL url, CheckedFunction<InputStream, T, IOException> checkedFunction) {
        String archive = getArchiveReference(url);
        try (ZipFile zip = new ZipFile(archive)) {
            //Opens the bean archive and tries to find the index file
            ZipEntry entry = zip.getEntry(filename);
            if (entry != null) {
                LOGGER.trace("Index file found");
                return Optional.of(checkedFunction.apply(zip.getInputStream(entry)));
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.trace("The Index file is not valid: [{}]", archive);
        } catch (UnsupportedVersion ex) {
            LOGGER.trace("The version of Index file is not supported: [{}]", archive);
        } catch (IOException ex) {
            LOGGER.trace("Cannot get Index file from: [{}], [{}]", archive, ex.getMessage());
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
