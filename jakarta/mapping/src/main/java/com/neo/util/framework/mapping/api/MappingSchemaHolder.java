package com.neo.util.framework.mapping.api;

import java.util.Set;

/**
 * Class which holds all mapping which allows to dynamically reload them on runtime
 */
public interface MappingSchemaHolder {

    /**
     * Reloads all schemas from disk
     *
     * @throws IllegalArgumentException if one of the schemas can't be loaded correctly
     */
    void reload();

    /**
     * Fetches a specific mapping schema. May produce null when it doesn't exist.
     */

    MappingSchema fetchMappingSchema(String schema);

    /**
     * Returns a list of all loaded schemas
     */
    Set<String> getMappingSchemaNames();
}
