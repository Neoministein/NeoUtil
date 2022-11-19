package com.neo.util.framework.api;

import com.neo.util.common.impl.exception.ExceptionDetails;

public interface FrameworkConstants {

    String JSON_SCHEMA_LOCATION = "configuration/schema";

    ExceptionDetails EX_UNAUTHORIZED = new ExceptionDetails(
            "auth/unauthorized", "The current request is unauthorized",false);

    ExceptionDetails EX_FORBIDDEN = new ExceptionDetails(
            "auth/forbidden", "The current request is forbidden",false);

}
