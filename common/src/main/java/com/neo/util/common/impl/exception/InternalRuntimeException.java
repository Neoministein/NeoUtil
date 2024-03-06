package com.neo.util.common.impl.exception;

import java.text.MessageFormat;

/**
 * This class handles all Exceptions associated which can occur during the runtime of the application
 */
public class InternalRuntimeException extends RuntimeException {

    protected final String exceptionId;

    public InternalRuntimeException(ExceptionDetails exceptionDetails, Object... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), arguments));
        this.exceptionId = exceptionDetails.getExceptionId();
    }

    public InternalRuntimeException(Exception cause, ExceptionDetails exceptionDetails, Object... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), arguments), cause);
        this.exceptionId = exceptionDetails.getExceptionId();
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public ExternalRuntimeException asExternal() {
        return new ExternalRuntimeException(this);
    }
}
