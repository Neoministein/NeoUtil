package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

import java.util.List;

public final class LoopArrayNode extends NestedNode {

    private final String varName;
    private final ExpressionValue loopExpression;

    public LoopArrayNode(String filedName, String varName, ExpressionValue loopExpression, List<Node> children, ExpressionValue skipExpression) {
        super(JsonDataType.ARRAY, filedName, children, skipExpression);
        this.varName = varName;
        this.loopExpression = loopExpression;
    }

    public String getVarName() {
        return varName;
    }

    public ExpressionValue getLoopExpression() {
        return loopExpression;
    }
}
