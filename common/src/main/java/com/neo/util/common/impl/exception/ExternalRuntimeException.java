package com.neo.util.common.impl.exception;

import java.text.MessageFormat;

public class ExternalRuntimeException extends RuntimeException {

    protected final String exceptionId;
    protected final String[] arguments;

    public ExternalRuntimeException(InternalRuntimeException internalRuntimeException) {
        super(internalRuntimeException.getMessage(), internalRuntimeException);
        this.exceptionId = internalRuntimeException.getExceptionId();
        this.arguments = internalRuntimeException.getArguments();
    }


    public ExternalRuntimeException(ExceptionDetails exceptionDetails, String... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), (Object[]) arguments));
        this.exceptionId = exceptionDetails.getExceptionId();
        this.arguments = arguments;
    }

    public ExternalRuntimeException(Exception cause, ExceptionDetails exceptionDetails, String... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), (Object[]) arguments), cause);
        this.exceptionId = exceptionDetails.getExceptionId();
        this.arguments = arguments;
    }

    public String getExceptionId() {
        return exceptionId;
    }
}
