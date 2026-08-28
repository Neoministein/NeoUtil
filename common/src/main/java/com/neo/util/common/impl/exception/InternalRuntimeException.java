package com.neo.util.common.impl.exception;

import java.text.MessageFormat;

/**
 * This class handles all Exceptions associated which can occur during the runtime of the application
 */
public class InternalRuntimeException extends RuntimeException {

    protected final String exceptionId;
    protected final String[] arguments;

    public InternalRuntimeException(ExceptionDetails exceptionDetails, String... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), (Object[]) arguments));
        this.exceptionId = exceptionDetails.getExceptionId();
        this.arguments = arguments;
    }

    public InternalRuntimeException(Exception cause, ExceptionDetails exceptionDetails, String... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), (Object[]) arguments), cause);
        this.exceptionId = exceptionDetails.getExceptionId();
        this.arguments = arguments;
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public String[] getArguments() {
        return arguments;
    }

    public ExternalRuntimeException asExternal() {
        return new ExternalRuntimeException(this);
    }

    public boolean isOffType(ExceptionDetails exceptionDetails) {
        return exceptionId.equals(exceptionDetails.getExceptionId());
    }
}
