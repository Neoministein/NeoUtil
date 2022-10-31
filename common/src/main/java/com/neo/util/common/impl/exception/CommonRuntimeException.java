package com.neo.util.common.impl.exception;

import java.text.MessageFormat;
import java.util.List;

/**
 * This class handles all Exceptions associated which can occur during the runtime of the application
 */
public class CommonRuntimeException extends RuntimeException {

    protected final boolean internal;
    protected final String exceptionId;
    protected final List<Object> arguments;

    protected CommonRuntimeException(String message) {
        super(message);
        this.internal = true;
        this.exceptionId = null;
        this.arguments = List.of();
    }

    protected CommonRuntimeException(String message, Exception exception) {
        super(message, exception);
        this.internal = true;
        this.exceptionId = null;
        this.arguments = List.of();
    }


    public CommonRuntimeException(ExceptionDetails exceptionDetails, Object... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), arguments));
        this.internal = exceptionDetails.isInternal();
        this.exceptionId = exceptionDetails.getExceptionId();
        this.arguments = List.of(arguments);
    }

    public CommonRuntimeException(Exception cause, ExceptionDetails exceptionDetails, Object... arguments) {
        super(MessageFormat.format(exceptionDetails.getFormat(), arguments), cause);
        this.internal = exceptionDetails.isInternal();
        this.exceptionId = exceptionDetails.getExceptionId();
        this.arguments = List.of(arguments);
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public boolean getInternal() {
        return internal;
    }
}
