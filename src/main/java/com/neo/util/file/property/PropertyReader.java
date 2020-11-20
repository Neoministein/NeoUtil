package com.neo.util.file.property;

import com.neo.util.file.FileReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class PropertyReader extends FileReader {

    public static Properties readPropertyFile(String location) throws IOException{
        Properties properties = new Properties();
        try {
            Reader reader = getReader(location);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            fileNotfound(location);
            throw new IOException(e);
        }
        return properties;
    }
}
