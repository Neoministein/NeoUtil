package com.neoutil.file.property;

import com.neoutil.file.FileReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class PropertyReader extends FileReader {

    public static Properties readPropertyFile(String location){
        Properties properties = new Properties();
        try {
            Reader reader = getReader(location);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            fileNotfound(location);
        }
        return properties;
    }
}
