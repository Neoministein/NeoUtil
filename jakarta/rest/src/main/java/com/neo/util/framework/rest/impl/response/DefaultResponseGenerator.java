package com.neo.util.framework.rest.impl.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.response.ResponseGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class DefaultResponseGenerator implements ResponseGenerator {

    @Override
    public Response error(int code, String errorCode, String message) {
        return Response.status(code).entity(errorObject(errorCode, message).toString()).build();
    }

    @Override
    public Response error(int code, CommonRuntimeException exceptionDetails) {
        return Response.status(code).entity(errorObject(exceptionDetails.getExceptionId(), exceptionDetails.getMessage()).toString()).build();
    }

    @Override
    public Response error(int code, ExceptionDetails exceptionDetails, Object... arguments) {
        return error(code, new CommonRuntimeException(exceptionDetails, arguments));
    }

    public ObjectNode errorObject(String errorCode, String message) {
        ObjectNode errorObject = JsonUtil.emptyObjectNode();
        errorObject.put("code", errorCode);
        errorObject.put("message", message);
        return errorObject;
    }
}
