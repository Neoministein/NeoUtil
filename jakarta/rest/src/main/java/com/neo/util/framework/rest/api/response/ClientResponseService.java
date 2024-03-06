package com.neo.util.framework.rest.api.response;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.common.impl.exception.InternalRuntimeException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

/**
 * This interface defines common response which can look different based on applications
 */
public interface ClientResponseService {

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
    Response error(int code, ExternalRuntimeException runtimeException);

    /**
     * A response with the status code and data in the response body
     *
     * @param code the status code
     * @param runtimeException the exception to cause the error
     * @return the response
     */
    Response error(int code, InternalRuntimeException runtimeException);

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
     *
     * @param mediaType
     * @return
     */
    Optional<ClientResponseGenerator> getGenerator(MediaType mediaType);
}
