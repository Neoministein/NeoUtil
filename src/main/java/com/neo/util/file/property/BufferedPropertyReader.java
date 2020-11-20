package com.neo.util.file.property;

import com.neo.util.logging.Logging;
import com.neo.util.logging.Multilogger;

import java.io.IOException;
import java.util.Properties;

public class BufferedPropertyReader {

    private final Logging logging = Multilogger.getInstance();

    private final Properties buffer;

    public BufferedPropertyReader(String location) throws IOException {
        try {
            buffer = PropertyReader.readPropertyFile(location);
        }catch (IOException e) {
            logging.println(Logging.ERROR,"Unable to read file");
            throw new IOException(e);
        }
    }

    private Object getValue(String name, Class<?> type) {
        String value = buffer.getProperty(name);
        if (value == null)
            throw new IllegalArgumentException("Missing configuration value: " + name);
        if (type == String.class)
            return value;
        if (type == boolean.class)
            return Boolean.parseBoolean(value);
        if (type == int.class)
            return Integer.parseInt(value);
        if (type == float.class)
            return Float.parseFloat(value);
        throw new IllegalArgumentException("Unknown configuration value type: " + type.getName());
    }

    public Properties getBuffer() {
        return buffer;
    }

    public String getString(String key) {
        return (String) getValue(key, String.class);
    }

    public boolean getBoolan(String key) {
        return (boolean) getValue(key, boolean.class);
    }

    public int getInt(String key){
        return (int) getValue(key, int.class);
    }

    public float getFloat(String key) {
        return (float) getValue(key, float.class);
    }
}
