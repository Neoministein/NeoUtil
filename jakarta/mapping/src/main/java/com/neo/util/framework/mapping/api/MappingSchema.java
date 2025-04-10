package com.neo.util.framework.mapping.api;

import com.neo.util.framework.mapping.api.node.*;
import com.networknt.schema.JsonSchema;

public class MappingSchema {


    private final NestedObjectNode schemaNode;
    private final JsonSchema inputSchema;

    public MappingSchema(NestedObjectNode schemaNode, JsonSchema inputSchema) {
        this.schemaNode = schemaNode;
        this.inputSchema = inputSchema;
    }

    public NestedObjectNode getMappingSchema() {
        return schemaNode;
    }

    public JsonSchema getInputSchema() {
        return inputSchema;
    }
}
