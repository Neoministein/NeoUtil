package com.neo.util.framework.api.config;

public interface ConfigProvider {

    /**
     * Returns a {@link Config} associated to the key.
     */
    Config get(String key);

    int getPriorty();
}
