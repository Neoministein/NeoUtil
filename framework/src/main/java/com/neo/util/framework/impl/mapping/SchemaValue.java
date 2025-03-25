package com.neo.util.framework.impl.mapping;

public class SchemaValue {

    public enum Type {
        STATIC,
        EXPRESSION
    }

    private final Type type;
    private final String value;

    public SchemaValue(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
