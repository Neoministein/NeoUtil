package com.neo.util.javax.impl.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.common.impl.json.JsonUtil;

import javax.ws.rs.core.Response;

public class DefaultResponse {

    private DefaultResponse() {}

    public static ObjectNode defaultResponse(int code, RequestContext context) {
        ObjectNode responseMessage = JsonUtil.emptyObjectNode();
        responseMessage.put("status", code);
        responseMessage.put("apiVersion", "1.0");
        responseMessage.put("context", context.toString());
        return responseMessage;
    }

    public static Response partialSuccess(int code, ObjectNode error, JsonNode data, RequestContext context) {
        ObjectNode response = DefaultResponse.defaultResponse(code, context);
        response.putIfAbsent("data", data);
        response.putIfAbsent("error", error);
        return Response.ok().entity(response).build();
    }

    public static Response success(RequestContext context) {
        return Response.ok().entity(defaultResponse(200, context).toString()).build();
    }

    public static Response success(RequestContext context, JsonNode data) {
        ObjectNode responseMessage = defaultResponse(200 ,context);
        responseMessage.putIfAbsent("data", data);

        return Response.ok().entity(responseMessage.toString()).build();
    }

    public static Response error(int code, RequestContext context, String errorCode, String message) {
        ObjectNode response = defaultResponse(code, context);
        response.putIfAbsent("error", errorObject(errorCode, message));

        return Response.ok().entity(response.toString()).build();
    }

    public static Response error(int code, ObjectNode error, RequestContext context) {
        ObjectNode response = defaultResponse(code, context);
        response.putIfAbsent("error", error);

        return Response.ok().entity(response.toString()).build();
    }

    public static ObjectNode errorObject(String errorCode, Exception ex) {
        return errorObject(errorCode, ex.getMessage());
    }

    public static ObjectNode errorObject(String errorCode, String message) {
        ObjectNode errorObject = JsonUtil.emptyObjectNode();
        errorObject.put("code", errorCode);
        errorObject.put("message", message);
        return errorObject;
    }
}
