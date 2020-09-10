package com.neoutil.file.property;

import com.neoutil.file.FileWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class PropertyWriter extends FileWriter {

    public static void writePropertyFile(Properties prop,String location){
        prop.putAll(PropertyReader.readPropertyFile(location));
        try {
            Writer writer = getWriter(location, false);
            prop.store(writer, null);
            writer.close();
        } catch (IOException ex) {
            fileNotFound(location, ex);
        }
    }
}
