package com.neo.util.framework.api.config;

/**
 * This class provided is an interface for the implementation of a tree-structured config system
 * <p>
 * The config is tree
 */
public interface ConfigService {

    /**
     * Returns a {@link Config} associated to the key.
     */
    Config get(String key);

    /**
     * Persists the given {@link ConfigValue}.
     */
    void save(ConfigValue<?> configValue);

}
