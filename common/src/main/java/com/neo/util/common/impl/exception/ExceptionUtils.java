package com.neo.util.common.impl.exception;

/**
 * A utility class for exceptions
 */
public class ExceptionUtils {

    private ExceptionUtils() {}

    /**
     * Checks of the exception is external and create a new {@link CommonRuntimeException} is if not.
     *
     * @param commonRuntimeException to check if it's external
     * @return an external {@link CommonRuntimeException}
     *
     */
    public static CommonRuntimeException asExternal(CommonRuntimeException commonRuntimeException) {
        if (commonRuntimeException.getInternal()) {
            return new CommonRuntimeException(
                    new ExceptionDetails(commonRuntimeException.getExceptionId(), commonRuntimeException.getMessage(), false)
            );
        }
        return commonRuntimeException;
    }
}
