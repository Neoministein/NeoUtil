package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

import java.util.List;

public abstract class NestedNode extends Node {

    private final List<Node> children;

    protected NestedNode(JsonDataType dataType, String filedName, List<Node> children, ExpressionValue skipExpression) {
        super(dataType, filedName, skipExpression);
        this.children = children;
    }

    public List<Node> getChildren() {
        return children;
    }
}
