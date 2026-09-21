package com.neo.util.jakarta.mapping.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.jakarta.mapping.expression.ExpressionValue;

import java.util.List;

public final class SingleArrayNode extends NestedNode {

    public SingleArrayNode(String filedName, List<Node> children, ExpressionValue defaultValue, ExpressionValue skipExpression) {
        super(JsonDataType.ARRAY, filedName, children, defaultValue, skipExpression);
    }
}
