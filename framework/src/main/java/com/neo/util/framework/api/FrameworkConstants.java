package com.neo.util.framework.api;

import com.neo.util.common.impl.exception.ExceptionDetails;

public final class FrameworkConstants {

    public static final String JSON_SCHEMA_LOCATION = "configuration/schema";

    public static final String JSON_SCHEMA_INDEX = "configuration/schema/schema-index.txt";

    public static final ExceptionDetails EX_UNAUTHORIZED = new ExceptionDetails(
            "auth/unauthorized", "The current request is unauthorized",false);

    public static final ExceptionDetails EX_FORBIDDEN = new ExceptionDetails(
            "auth/forbidden", "The current request is forbidden",false);

    private FrameworkConstants() {}

}
