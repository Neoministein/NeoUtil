package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

import java.util.List;

public final class LoopArrayNode extends NestedNode {

    private final String varName;
    private final String loopPath;

    public LoopArrayNode(String filedName, String varName, String loopPath, List<Node> children) {
        super(JsonDataType.ARRAY, filedName, children);
        this.varName = varName;
        this.loopPath = loopPath;
    }

    public String getVarName() {
        return varName;
    }

    public String getLoopPath() {
        return loopPath;
    }
}
