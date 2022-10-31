package com.neo.util.common.impl.exception;

/**
 * This class handles all Exceptions associated with misconfiguration of the application
 */
public class ConfigurationException extends CommonRuntimeException {

    public ConfigurationException(ExceptionDetails exceptionDetails, Object... arguments) {
        super(exceptionDetails, arguments);
    }

    public ConfigurationException(Exception cause, ExceptionDetails exceptionDetails, Object... arguments) {
        super(cause, exceptionDetails, arguments);
    }
}
