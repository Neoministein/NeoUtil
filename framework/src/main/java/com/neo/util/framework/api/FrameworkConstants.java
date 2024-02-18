package com.neo.util.framework.api;

import com.neo.util.common.impl.exception.ExceptionDetails;

public final class FrameworkConstants {

    public static final String JSON_SCHEMA_LOCATION = "configuration/schema/";

    public static final ExceptionDetails EX_UNAUTHORIZED = new ExceptionDetails(
            "auth/unauthorized", "The current request is unauthorized",false);

    public static final ExceptionDetails EX_FORBIDDEN = new ExceptionDetails(
            "auth/forbidden", "The current request is forbidden",false);

    public static final ExceptionDetails EX_UNSUPPORTED_AUTH_TYPE = new ExceptionDetails(
            "auth/unsupported", "The provided authentication type is unsupported",false);

    private FrameworkConstants() {}

}
