package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

import java.util.List;

public final class NestedObjectNode extends NestedNode {

    public NestedObjectNode(String filedName, List<Node> children, ExpressionValue defaultValue, ExpressionValue skipExpression) {
        super(JsonDataType.OBJECT, filedName, children, defaultValue, skipExpression);
    }
}
