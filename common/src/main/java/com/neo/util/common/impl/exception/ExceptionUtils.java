package com.neo.util.common.impl.exception;

public class ExceptionUtils {

    private ExceptionUtils() {}

    public static CommonRuntimeException asExternal(CommonRuntimeException commonRuntimeException) throws CommonRuntimeException {
        if (commonRuntimeException.getInternal()) {
            return new CommonRuntimeException(
                    new ExceptionDetails(commonRuntimeException.getExceptionId(), commonRuntimeException.getMessage(), true)
            );
        }
        return commonRuntimeException;
    }
}
