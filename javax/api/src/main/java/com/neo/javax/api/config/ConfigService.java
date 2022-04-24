package com.neo.javax.api.config;

/**
 * This class provided is a interface for the implementation of a CDI config system
 */
public interface ConfigService {

    /**
     * Returns new empty config
     * @return an empty Config
     */
    Config empty();

    /**
     * Returns the config associated to the key
     * @param key the key
     * @return the config
     */
    Config get(String key);

    /**
     * Persists the given config
     * @param config the config to persist
     */
    void save(Config config);

}
