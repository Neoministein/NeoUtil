package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

public abstract class Node {

    private final JsonDataType dataType;
    private final String filedName;

    protected Node(JsonDataType dataType, String filedName) {
        this.dataType = dataType;
        this.filedName = filedName;
    }

    public JsonDataType getDataType() {
        return dataType;
    }

    public String getFiledName() {
        return filedName;
    }

    @Override
    public String toString() {
        return filedName + "->" + dataType;
    }
}
