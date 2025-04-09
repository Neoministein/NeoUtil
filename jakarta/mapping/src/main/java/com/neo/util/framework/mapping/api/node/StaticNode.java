package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

public final class StaticNode extends Node {

    private final Object value;

    public StaticNode(JsonDataType dataType, String filedName, Object value, ExpressionValue skipExpression) {
        super(dataType, filedName, skipExpression);
        this.value = value;
    }

    public Object getSchemaValue() {
        return value;
    }
}
