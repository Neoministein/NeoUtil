package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

import java.util.List;

public abstract class NestedNode extends Node {

    private final List<Node> children;

    protected NestedNode(JsonDataType dataType, String filedName, List<Node> children) {
        super(dataType, filedName);
        this.children = children;
    }

    public List<Node> getChildren() {
        return children;
    }
}
