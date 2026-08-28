package com.neo.util.framework.mapping.api;

import com.neo.util.framework.mapping.api.node.NestedObjectNode;
import com.networknt.schema.Schema;

/**
 * Constant state holder for the SchemaMapping
 */
public class MappingSchema {

    private final String name;
    private final NestedObjectNode schemaNode;
    private final Schema inputSchema;

    public MappingSchema(String name, NestedObjectNode schemaNode, Schema inputSchema) {
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

    public Schema getInputSchema() {
        return inputSchema;
    }
}
