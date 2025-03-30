package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

public final class ExpressionNode extends Node {

    private final String expression;
    private final Object defaultValue;

    public ExpressionNode(JsonDataType dataType, String filedName, String expression, Object defaultValue) {
        super(dataType, filedName);
        this.expression = expression;
        this.defaultValue = defaultValue;
    }

    public String getExpression() {
        return expression;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
