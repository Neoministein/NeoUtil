package com.neo.util.common.impl.exception;

public final class ExceptionDetails {

    private final String exceptionId;
    private final String format;

    public ExceptionDetails(String exceptionId, String format) {
        this.exceptionId = exceptionId;
        this.format = format;
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public String getFormat() {
        return format;
    }
}
