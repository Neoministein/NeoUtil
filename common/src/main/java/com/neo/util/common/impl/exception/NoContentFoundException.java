package com.neo.util.common.impl.exception;

/**
 * This class handles all Exceptions associated with no content found
 */
public class NoContentFoundException extends InternalRuntimeException {

    public NoContentFoundException(ExceptionDetails exceptionDetails, Object... arguments) {
        super(exceptionDetails, arguments);
    }

    public NoContentFoundException(Exception cause, ExceptionDetails exceptionDetails, Object... arguments) {
        super(cause, exceptionDetails, arguments);
    }
}
