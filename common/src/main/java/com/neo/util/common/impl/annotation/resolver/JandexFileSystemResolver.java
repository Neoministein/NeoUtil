package com.neo.util.common.impl.annotation.resolver;

import com.neo.util.common.api.annoation.JandexResolver;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * The Jandex Files is in a FileSystem, so most likely a development environment.
 * The Jandex file won't be read. All .class files will be scanned
 */
public class JandexFileSystemResolver implements JandexResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JandexFileSystemResolver.class);

    public static boolean isClass(String name) {
        return name.endsWith(CLASS_FILE_EXTENSION);
    }

    @Override
    public Optional<Index> getIndex(URL url) {
        String path = url.getPath();

        if (path.endsWith(JANDEX_FILE_NAME)) {
            path = path.substring(0, path.length() - JANDEX_INDEX_NAME.length());
        }

        Indexer indexer = new Indexer();
        boolean nested = false;
        File file;

        if (path.contains(JAR_URL_SEPARATOR)) {
            // Most probably a nested archive, e.g. "/home/duke/duke.jar!/lib/foo.jar"
            file = new File(path.substring(0, path.indexOf(JAR_URL_SEPARATOR)));
            nested = true;
        } else {
            file = new File(path);
        }

        if(!file.canRead()) {
            return Optional.empty();
        }

        try {
            if (file.isDirectory()) {
                handleDirectory(new DirectoryEntry().setFile(file), indexer);
            } else {
                if(nested) {
                    handleNestedFile(path, file, indexer);
                } else {
                    handleFile(file, indexer);
                }
            }
        } catch (Exception ex) {
            LOGGER.trace("Cannot get Jandex index file from: [{}], [{}]", path, ex.getMessage());
            return Optional.empty();
        }
        return Optional.of(indexer.complete());
    }



    protected void handleFile(File file, Indexer indexer) throws IOException {
        LOGGER.trace("Handle archive file: [{}]", file);
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ZipFileEntry entry = new ZipFileEntry(PROTOCOL_JAR + ":" + file.toURI().toURL().toExternalForm() + JAR_URL_SEPARATOR);
            while (entries.hasMoreElements()) {
                add(entry.setName(entries.nextElement().getName()), indexer);
            }
        }
    }

    protected void handleDirectory(DirectoryEntry entry, Indexer indexer) throws IOException {
        LOGGER.trace("Handle archive file: [{}]", entry.getFile());
        File[] files = entry.getFile().listFiles();
        String parentPath = entry.getName();
        for (File child : files) {
            if(entry.getName() != null ) {
                entry.setPath(entry.getName() + "/" + child.getName());
            } else {
                entry.setPath(child.getName());
            }
            entry.setFile(child);
            if (child.isDirectory()) {
                handleDirectory(entry, indexer);
            } else {
                add(entry, indexer);
            }
            entry.setPath(parentPath);
        }
    }

    protected void handleNestedFile(String path, File file, Indexer indexer) throws IOException {
        LOGGER.trace("Handle nested archive  File: [{}]  Path: [{}]", file, path);

        String nestedEntryName = path.substring(path.indexOf(JAR_URL_SEPARATOR) + JAR_URL_SEPARATOR.length());
        if (nestedEntryName.contains(JAR_URL_SEPARATOR)) {
            throw new IllegalArgumentException("Recursive nested archives are not supported");
        }

        try (ZipFile zip = new ZipFile(file)) {

            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {

                ZipEntry zipEntry = entries.nextElement();

                if (zipEntry.getName().equals(nestedEntryName)) {
                    // Nested jar entry
                    ZipFileEntry entry = getZipFileEntry(file, zipEntry);
                    // Add entries from the nested archive
                    try (ZipInputStream nestedZip = new ZipInputStream(zip.getInputStream(zipEntry))) {
                        ZipEntry nestedEntry;
                        while ((nestedEntry = nestedZip.getNextEntry()) != null) {
                            add(entry.setName(nestedEntry.getName()), indexer);
                        }
                    }
                } else if (zipEntry.getName().startsWith(nestedEntryName)) {
                    // Nested file entries
                    add(getZipFileEntry(file, zipEntry).setName(zipEntry.getName().substring(nestedEntryName.length() + 1)), indexer);
                }
            }
        }
    }

    private ZipFileEntry getZipFileEntry(File file, ZipEntry zipEntry) throws MalformedURLException {
        // Reconstruct the archive URL. It might be like either of the following:
        // "jar:file:/home/duke/duke.jar!/classes"
        // "jar:file:/home/duke/duke.jar!/lib/foo.jar"
        return new ZipFileEntry(PROTOCOL_JAR + ":" + file.toURI().toURL().toExternalForm() + JAR_URL_SEPARATOR + zipEntry.getName());
    }

    protected void add(Entry entry, Indexer indexer) throws IOException {
        if (isClass(entry.getName())) {
            entry.indexEntry(indexer);
        }
    }

    /**
     * An abstraction of a bean archive entry.
     */
    protected interface Entry {

        String getName();

        void indexEntry(Indexer indexer) throws IOException;

    }

    private static class ZipFileEntry implements Entry {

        private String name;

        private String archiveUrl;

        ZipFileEntry(String archiveUrl) {
            this.archiveUrl = archiveUrl;
        }

        @Override
        public String getName() {
            return name;
        }

        public void indexEntry(Indexer indexer) throws IOException {
            try (ZipFile zip = new ZipFile(archiveUrl)) {
                // Open the bean archive and try to find the index file
                ZipEntry entry = zip.getEntry(name);
                if (entry != null) {
                    indexer.index(zip.getInputStream(entry));
                }
            }
        }


        ZipFileEntry setName(String name) {
            this.name = name;
            return this;
        }

    }

    private static class DirectoryEntry implements Entry {

        private String path;

        private File file;

        @Override
        public String getName() {
            return path;
        }

        @Override
        public void indexEntry(Indexer indexer) throws IOException {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                indexer.index(in);
            }
        }

        public DirectoryEntry setPath(String path) {
            this.path = path;
            return this;
        }

        public File getFile() {
            return file;
        }

        public DirectoryEntry setFile(File dir) {
            this.file = dir;
            return this;
        }
    }
}
