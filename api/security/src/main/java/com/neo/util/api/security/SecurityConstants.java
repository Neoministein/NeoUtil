package com.neo.util.api.security;

import com.neo.util.common.impl.exception.ExceptionDetails;

public final class SecurityConstants {

    public static final ExceptionDetails EX_UNAUTHORIZED = new ExceptionDetails(
            "auth/unauthorized", "The current request is unauthorized");

    public static final ExceptionDetails EX_FORBIDDEN = new ExceptionDetails(
            "auth/forbidden", "The current request is forbidden");

    public static final ExceptionDetails EX_UNSUPPORTED_AUTH_TYPE = new ExceptionDetails(
            "auth/unsupported", "The provided authentication type is unsupported");
}
