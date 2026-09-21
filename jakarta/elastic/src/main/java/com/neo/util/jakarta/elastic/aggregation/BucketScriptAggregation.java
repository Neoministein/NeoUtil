package com.neo.util.jakarta.elastic.aggregation;

import com.neo.util.api.persistence.query.aggregation.SearchAggregation;

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
