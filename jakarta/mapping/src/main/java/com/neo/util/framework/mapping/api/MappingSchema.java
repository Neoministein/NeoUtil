package com.neo.util.framework.mapping.api;

import com.neo.util.framework.mapping.api.node.*;
import com.networknt.schema.JsonSchema;

/**
 * Constant state holder for the SchemaMapping
 */
public class MappingSchema {

    private final String name;
    private final NestedObjectNode schemaNode;
    private final JsonSchema inputSchema;

    public MappingSchema(String name, NestedObjectNode schemaNode, JsonSchema inputSchema) {
        this.name = name;
        this.schemaNode = schemaNode;
        this.inputSchema = inputSchema;
    }

    public String getName() {
        return name;
    }

    public NestedObjectNode getMappingSchema() {
        return schemaNode;
    }

    public JsonSchema getInputSchema() {
        return inputSchema;
    }
}
