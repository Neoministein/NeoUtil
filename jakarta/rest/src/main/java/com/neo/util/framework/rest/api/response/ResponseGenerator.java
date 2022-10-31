package com.neo.util.framework.rest.api.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import jakarta.ws.rs.core.Response;

/**
 * This interface defines common response which can look different based on applications
 */
public interface ResponseGenerator {

    /**
     * @return a default 200 response
     */
    Response success();

    /**
     * A default 200 response with data in the response body
     *
     * @param data of the response body
     * @return the response
     */
    Response success(JsonNode data);

    /**
     * A response with the status code and data in the response body
     *
     * @param code the status code
     * @param data of the response body
     * @return the response
     */
    Response buildResponse(int code, JsonNode data);

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
}
