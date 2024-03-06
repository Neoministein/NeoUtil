package com.neo.util.common.impl.exception;

import java.text.MessageFormat;

public class ExternalRuntimeException extends RuntimeException {

    protected final String exceptionId;

    public ExternalRuntimeException(InternalRuntimeException internalRuntimeException) {
        super(internalRuntimeException.getMessage(), internalRuntimeException);
        this.exceptionId = internalRuntimeException.getExceptionId();
    }


    public ExternalRuntimeException(ExceptionDetails exceptionDetails, Object... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), arguments));
        this.exceptionId = exceptionDetails.getExceptionId();
    }

    public ExternalRuntimeException(Exception cause, ExceptionDetails exceptionDetails, Object... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), arguments), cause);
        this.exceptionId = exceptionDetails.getExceptionId();
    }

    public String getExceptionId() {
        return exceptionId;
    }
}
