package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.RequestScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequestScoped  // Ensures it's a singleton in the app
public class MappingStateHolder {

    private ObjectNode sourceJson;
    private Map<String, Object> loopValues;
    private Map<Object, Integer> loopIteration;

    public void init(ObjectNode sourceJson) {
        this.sourceJson = sourceJson;
        this.loopValues = new HashMap<>();
        this.loopIteration = new HashMap<>();
    }

    public void setLoopIteration(Integer iteration, String varName, Object value) {
        loopValues.put(varName, value);
        loopIteration.put(value, iteration);
    }

    public void removeLoopIteration(String varName) {
        Object value = loopValues.remove(varName);
        if (value != null) {
            loopIteration.remove(value);
        }
    }

    public Optional<Object> getValueFromInput(String path) {
        Object loopValue = loopValues.get(path);
        if (loopValue != null) {
            return Optional.ofNullable(loopValue);
        }
        JsonNode node = sourceJson.path(path);
        if (node.isMissingNode()) {
            return Optional.empty();
        }
        return Optional.of(node);
    }

    public Optional<Integer> getLoopIteration(Object value) {
        return Optional.ofNullable(loopIteration.get(value));
    }
}
