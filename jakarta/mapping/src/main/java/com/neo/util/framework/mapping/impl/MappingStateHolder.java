package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.framework.mapping.api.MappingSchema;
import jakarta.enterprise.context.RequestScoped;

import java.util.Optional;

@RequestScoped  // Ensures it's a singleton in the app
public class MappingStateHolder {

    private MappingSchema schema;
    private JsonNode inputNode;

    public void setSchema(MappingSchema mappingSchema, JsonNode inputNode) {
        this.schema = mappingSchema;
        inputNode = inputNode;
    }

    public Optional<JsonNode> getValueFromMapping(String value) {
        JsonNode node = schema.getNode(value);
        if (node.isMissingNode()) {
            return Optional.empty();
        }

        return Optional.of(node);
    }

}
