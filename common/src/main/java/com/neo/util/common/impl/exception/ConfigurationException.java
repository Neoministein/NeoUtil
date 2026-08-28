package com.neo.util.common.impl.exception;

/**
 * This class handles all Exceptions associated with misconfiguration of the application
 */
public class ConfigurationException extends InternalRuntimeException {

    public ConfigurationException(ExceptionDetails exceptionDetails, String... arguments) {
        super(exceptionDetails, arguments);
    }

    public ConfigurationException(Exception cause, ExceptionDetails exceptionDetails, String... arguments) {
        super(cause, exceptionDetails, arguments);
    }
}
