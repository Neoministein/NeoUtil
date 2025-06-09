package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.RequestScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequestScoped
public class MappingStateHolder {

    private JsonNode sourceJson;
    private Map<String, Object> loopValues;
    private Map<Object, Integer> loopIteration;
    private MappingStateHolder parent;

    protected MappingStateHolder(MappingStateHolder parent) {
        this.sourceJson = parent.sourceJson;
        this.loopValues = parent.loopValues;
        this.loopIteration = parent.loopIteration;
        this.parent = null;
    }

    protected MappingStateHolder() {}

    public void init(JsonNode sourceJson) {
        //To Allow nested mapping we save the current information into a different object and load it back when we say that the mapping is done.
        //This will break if mapping processing will become multithreaded
        if (sourceJson != null) {
            this.parent = new MappingStateHolder(this);
        }

        this.sourceJson = sourceJson;
        this.loopValues = new HashMap<>();
        this.loopIteration = new HashMap<>();
    }

    public void clear() {
        if (parent != null) {
            this.sourceJson = parent.sourceJson;
            this.loopValues = parent.loopValues;
            this.loopIteration = parent.loopIteration;
        }
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

    public Object getValueFromInput(String path) {
        Object loopValue = loopValues.get(path);
        if (loopValue != null) {
            return loopValue;
        }
        return sourceJson.path(path);
    }

    public Optional<Integer> getLoopIteration(Object value) {
        return Optional.ofNullable(loopIteration.get(value));
    }
}
