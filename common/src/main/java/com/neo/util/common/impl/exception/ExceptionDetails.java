package com.neo.util.common.impl.exception;

public class ExceptionDetails {

    protected final String exceptionId;
    protected final String format;
    protected final boolean internal;

    public ExceptionDetails(String exceptionId, String format, boolean internal) {
        this.exceptionId = exceptionId;
        this.format = format;
        this.internal = internal;
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public String getFormat() {
        return format;
    }

    public boolean isInternal() {
        return internal;
    }
}
