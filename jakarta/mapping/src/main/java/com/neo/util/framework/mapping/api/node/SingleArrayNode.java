package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

import java.util.List;

public final class SingleArrayNode extends NestedNode {

    public SingleArrayNode(String filedName, List<Node> children, ExpressionValue skipExpression) {
        super(JsonDataType.ARRAY, filedName, children, skipExpression);
    }
}
