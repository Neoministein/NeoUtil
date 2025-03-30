package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

import java.util.List;

public final class NestedObjectNode extends NestedNode {

    public NestedObjectNode(String filedName, List<Node> children) {
        super(JsonDataType.OBJECT, filedName, children);
    }
}
