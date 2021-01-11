package com.neo.util.file.property;

import com.neo.util.file.FileWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class PropertyWriter {

    private PropertyWriter() {}

    public static void writePropertyFile(Properties prop,String location) throws IOException {
        Properties properties = PropertyReader.readPropertyFile(location);
        properties.putAll(prop);
        try {
            Writer writer = FileWriter.getWriter(location, false);
            properties.store(writer, null);
            writer.close();
        } catch (IOException ex) {
            FileWriter.fileNotFound(location, ex);
            throw new IOException(ex);
        }
    }
}
