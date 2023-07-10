package com.neo.util.framework.rest.api.response;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import jakarta.ws.rs.core.Response;

/**
 * This interface defines common response which can look different based on applications
 */
public interface ResponseGenerator {

    /**
     * A header that should be added to a valid error response object
     */
    String VALID_BACKEND_ERROR = "validBackendError";

    /**
     * A response with the status code and data in the response body
     *
     * @param code the status code
     * @param runtimeException the exception to cause the error
     * @return the response
     */
    Response error(int code, CommonRuntimeException runtimeException);

    /**
     * A response with the status code and data in the response body
     *
     * @param code the status code
     * @param exceptionDetails the exception to cause the error
     * @param arguments the arguments of the exception details
     * @return the response
     */
    Response error(int code, ExceptionDetails exceptionDetails, Object... arguments);

    /**
     * A response with the status code and data in the response body
     *
     * @param code the status code
     * @param errorCode the error code in the response body
     * @param message the error message in the response body
     * @return the response
     */
    Response error(int code, String errorCode, String message);

    /**
     * Retries to retrieve the error code from the given response object.
     *
     * @param entity the response entity
     * @return the error code or {@link com.neo.util.common.impl.StringUtils#EMPTY}
     */
    String responseToErrorCode(Object entity);
}
