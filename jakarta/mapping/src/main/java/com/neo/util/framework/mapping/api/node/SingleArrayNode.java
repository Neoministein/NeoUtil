package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;

import java.util.List;

public final class SingleArrayNode extends NestedNode {

    public SingleArrayNode(String filedName, List<Node> children) {
        super(JsonDataType.ARRAY, filedName, children);
    }
}
