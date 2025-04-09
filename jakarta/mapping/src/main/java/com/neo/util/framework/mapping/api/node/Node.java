package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

import java.util.Optional;

public abstract class Node {

    private final JsonDataType dataType;
    private final String filedName;
    private final ExpressionValue skipExpression;

    protected Node(JsonDataType dataType, String filedName, ExpressionValue skipExpression) {
        this.dataType = dataType;
        this.filedName = filedName;
        this.skipExpression = skipExpression;
    }

    public JsonDataType getDataType() {
        return dataType;
    }

    public String getFiledName() {
        return filedName;
    }

    public Optional<ExpressionValue> getSkipExpression() {
        return Optional.ofNullable(skipExpression);
    }

    @Override
    public String toString() {
        return filedName + "->" + dataType;
    }
}
