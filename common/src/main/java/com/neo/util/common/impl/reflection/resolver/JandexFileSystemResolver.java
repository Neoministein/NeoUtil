package com.neo.util.common.impl.reflection.resolver;

import com.neo.util.common.api.func.CheckedConsumer;
import com.neo.util.common.api.reflection.IndexResolver;
import com.neo.util.common.impl.reflection.Rendex;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * The Jandex Files is in a FileSystem, so most likely a development environment.
 * The Jandex file won't be read. All .class files will be scanned
 */
public class JandexFileSystemResolver implements IndexResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JandexFileSystemResolver.class);

    public static boolean isClass(String name) {
        return name.endsWith(CLASS_FILE_EXTENSION);
    }

    @Override
    public Optional<Index> getJandex(URL url) {
        String path = url.getPath();

        if (path.endsWith(JANDEX_FILE_NAME)) {
            path = path.substring(0, path.length() - JANDEX_INDEX_NAME.length());
        }

        AtomicBoolean checkedConsumerCalled = new AtomicBoolean(false);
        Indexer indexer = new Indexer();
        
        fileSystemResolver(path, entry -> {
            checkedConsumerCalled.set(true);
            if (isClass(entry.getName())) {
                entry.indexEntry(indexer);
            }
        });
        
        if (checkedConsumerCalled.get()) {
            return Optional.of(indexer.complete());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Rendex> getRendex(URL url) {
        String path = url.getPath();

        if (path.endsWith(RENDEX_FILE_NAME)) {
            path = path.substring(0, path.length() - RENDEX_INDEX_NAME.length());
        }
        AtomicBoolean checkedConsumerCalled = new AtomicBoolean(false);
        Rendex rendex = new Rendex();

        fileSystemResolver(path, entry -> {
            checkedConsumerCalled.set(true);
            if (!isClass(entry.getName())) {
                entry.indexEntry(rendex);
            }
        });

        if (checkedConsumerCalled.get()) {
            return Optional.of(rendex);
        }
        return Optional.empty();
    }

    protected void fileSystemResolver(String path, CheckedConsumer<Entry, IOException> entryCheckedConsumer) {
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
            return;
        }

        try {
            if (file.isDirectory()) {
                handleDirectory(new DirectoryEntry().setFile(file), entryCheckedConsumer);
            } else {
                if(nested) {
                    handleNestedFile(path, file, entryCheckedConsumer);
                } else {
                    handleFile(file, entryCheckedConsumer);
                }
            }
        } catch (Exception ex) {
            LOGGER.trace("Cannot get Jandex index file from: [{}], [{}]", path, ex.getMessage());
        }
    }


    protected void handleFile(File file, CheckedConsumer<Entry, IOException> entryCheckedConsumer) throws IOException {
        LOGGER.trace("Handle archive file: [{}]", file);
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ZipFileEntry entry = new ZipFileEntry(PROTOCOL_JAR + ":" + file.toURI().toURL().toExternalForm() + JAR_URL_SEPARATOR);
            while (entries.hasMoreElements()) {
                add(entry.setName(entries.nextElement().getName()), entryCheckedConsumer);
            }
        }
    }

    protected void handleDirectory(DirectoryEntry entry, CheckedConsumer<Entry, IOException> entryCheckedConsumer) throws IOException {
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
                handleDirectory(entry, entryCheckedConsumer);
            } else {
                add(entry, entryCheckedConsumer);
            }
            entry.setPath(parentPath);
        }
    }

    protected void handleNestedFile(String path, File file, CheckedConsumer<Entry, IOException> entryCheckedConsumer) throws IOException {
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
                            add(entry.setName(nestedEntry.getName()), entryCheckedConsumer);
                        }
                    }
                } else if (zipEntry.getName().startsWith(nestedEntryName)) {
                    // Nested file entries
                    add(getZipFileEntry(file, zipEntry).setName(zipEntry.getName().substring(nestedEntryName.length() + 1)), entryCheckedConsumer);
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

    protected void add(Entry entry, CheckedConsumer<Entry, IOException> entryCheckedConsumer) throws IOException {
        entryCheckedConsumer.accept(entry);
    }

    /**
     * An abstraction of a bean archive entry.
     */
    protected interface Entry {

        String getName();

        void indexEntry(Indexer indexer) throws IOException;

        default void indexEntry(Rendex rendex) throws IOException {
            rendex.addFile(getName());
        }

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
