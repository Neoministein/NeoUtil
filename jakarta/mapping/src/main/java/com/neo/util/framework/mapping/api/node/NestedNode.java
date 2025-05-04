package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

import java.util.List;
import java.util.Optional;

public abstract class NestedNode extends Node {

    private final List<Node> children;
    private final ExpressionValue defaultValue;

    protected NestedNode(JsonDataType dataType, String filedName, List<Node> children, ExpressionValue defaultValue, ExpressionValue skipExpression) {
        super(dataType, filedName, skipExpression);
        this.children = children;
        this.defaultValue = defaultValue;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Optional<ExpressionValue> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
}
