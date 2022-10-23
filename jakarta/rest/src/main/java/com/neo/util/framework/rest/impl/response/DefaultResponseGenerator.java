package com.neo.util.framework.rest.impl.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    public Response error(int code, String errorCode, String message) {
        return Response.status(code).entity(errorObject(errorCode, message).toString()).build();
    }

    @Override
    public Response error(int code, ObjectNode error) {

        return Response.status(code).entity(error.toString()).build();
    }

    @Override
    public ObjectNode errorObject(String errorCode, Exception ex) {
        return errorObject(errorCode, ex.getMessage());
    }

    @Override
    public ObjectNode errorObject(String errorCode, String message) {
        ObjectNode errorObject = JsonUtil.emptyObjectNode();
        errorObject.put("code", errorCode);
        errorObject.put("message", message);
        return errorObject;
    }
}
