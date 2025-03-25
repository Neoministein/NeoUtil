package com.neo.util.common.api.json;

public enum JsonDataType {

    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY;

    public static JsonDataType fromString(String dataType) {
        try {
            return JsonDataType.valueOf(dataType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex); //TODO
        }
    }
}
