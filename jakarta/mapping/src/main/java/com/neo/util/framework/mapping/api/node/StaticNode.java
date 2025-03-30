package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

public final class StaticNode extends Node {

    private final Object value;

    public StaticNode(JsonDataType dataType, String filedName, Object value) {
        super(dataType, filedName);
        this.value = value;
    }

    public Object getSchemaValue() {
        return value;
    }
}
