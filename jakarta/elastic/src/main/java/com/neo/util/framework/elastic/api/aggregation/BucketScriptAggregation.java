package com.neo.util.framework.elastic.api.aggregation;

import com.neo.util.framework.api.persistence.aggregation.SearchAggregation;

import java.util.Map;

public record BucketScriptAggregation(String name, String script, Map<String, String> path) implements SearchAggregation {


    @Override
    public String getName() {
        return name;
    }

    public String getScript() {
        return script;
    }

    public Map<String, String> getPath() {
        return path;
    }
}
