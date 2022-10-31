package com.neo.util.framework.rest.impl.response;

import com.fasterxml.jackson.databind.JsonNode;
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
    public Response success() {
        return Response.ok().build();
    }

    @Override
    public Response success(JsonNode data) {
        return Response.ok().entity(data.toString()).build();
    }

    @Override
    public Response buildResponse(int code, JsonNode data) {
        return Response.status(code).entity(data).build();
    }

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
