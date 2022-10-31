package com.neo.util.common.impl.exception;

/**
 * This class handles all Exceptions associated with invalid input
 */
public class ValidationException extends CommonRuntimeException {

    public ValidationException(ExceptionDetails exceptionDetails, Object... arguments) {
        super(exceptionDetails, arguments);
    }

    public ValidationException(Exception cause, ExceptionDetails exceptionDetails, Object... arguments) {
        super(cause, exceptionDetails, arguments);
    }
}
