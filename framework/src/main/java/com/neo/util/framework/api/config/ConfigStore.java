package com.neo.util.framework.api.config;

import java.util.List;
import java.util.Optional;

/**
 * This class provided is an interface for the implementation of a
 * tree-structured config system
 */
public interface ConfigStore extends Comparable<ConfigStore> {

    boolean isMutable();

    int getPriority();

    Optional<String> get(String key);

    Optional<List<String>> getList(String key);

    void save(String key, String value);

    @Override
    default int compareTo(ConfigStore o) {
        return getPriority() - o.getPriority();
    }
}
