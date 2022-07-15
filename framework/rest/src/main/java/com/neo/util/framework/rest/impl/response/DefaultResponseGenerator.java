package com.neo.util.framework.rest.impl.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class DefaultResponseGenerator implements ResponseGenerator {

    @Inject
    protected RequestDetails requestDetails;

    protected ObjectNode defaultResponse(int code) {
        ObjectNode responseMessage = JsonUtil.emptyObjectNode();
        responseMessage.put("status", code);
        responseMessage.put("apiVersion", "1.0");
        responseMessage.put("context", requestDetails.getRequestContext().toString());
        return responseMessage;
    }

    @Override
    public Response success() {
        return Response.ok().entity(defaultResponse(200).toString()).build();
    }

    @Override
    public Response success(JsonNode data) {
        ObjectNode responseMessage = defaultResponse(200);
        responseMessage.putIfAbsent("data", data);

        return Response.ok().entity(responseMessage.toString()).build();
    }

    @Override
    public Response partialSuccess(int code, JsonNode data, ObjectNode error) {
        ObjectNode response = defaultResponse(code);
        response.putIfAbsent("data", data);
        response.putIfAbsent("error", error);
        return Response.ok().entity(response).build();
    }

    @Override
    public Response error(int code, String errorCode, String message) {
        ObjectNode response = defaultResponse(code);
        response.putIfAbsent("error", errorObject(errorCode, message));

        return Response.status(code).entity(response.toString()).build();
    }

    @Override
    public Response error(int code, ObjectNode error) {
        ObjectNode response = defaultResponse(code);
        response.putIfAbsent("error", error);

        return Response.status(code).entity(response.toString()).build();
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
