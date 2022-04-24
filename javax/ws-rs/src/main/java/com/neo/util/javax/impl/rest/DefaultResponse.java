package com.neo.util.javax.impl.rest;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;

public class DefaultResponse {

    private DefaultResponse() {}

    public static JSONObject defaultResponse(int code, String context) {
        JSONObject responseMessage = new JSONObject();
        responseMessage.put("status", code);
        responseMessage.put("apiVersion", "1.0");
        responseMessage.put("context", context);
        return responseMessage;
    }

    public static Response partialSuccess(int code, JSONObject error ,JSONArray data, String context) {
        JSONObject response = DefaultResponse.defaultResponse(code, context);
        response.put("data", data);
        response.put("error", error);
        return Response.ok().entity(response).build();
    }

    public static Response success(String context) {
        return Response.ok().entity(defaultResponse(200, context).toString()).build();
    }

    public static Response success(String context ,JSONArray data) {
        JSONObject responseMessage = defaultResponse(200 ,context);
        responseMessage.put("data", data);

        return Response.ok().entity(responseMessage.toString()).build();
    }

    public static Response error(int code, String context, String errorCode, String message) {
        JSONObject response = defaultResponse(code, context);
        response.put("error", errorObject(errorCode, message));

        return Response.ok().entity(response.toString()).build();
    }

    public static Response error(int code, JSONObject error, String context) {
        JSONObject response = defaultResponse(code, context);
        response.put("error", error);

        return Response.ok().entity(response.toString()).build();
    }

    public static JSONObject errorObject(String errorCode, Exception ex) {
        return errorObject(errorCode, ex.getMessage());
    }

    public static JSONObject errorObject(String errorCode, String message) {
        JSONObject errorObject = new JSONObject();
        errorObject.put("error", errorCode);
        errorObject.put("message", message);
        return errorObject;
    }

    public static JSONArray errorArray(String errorCode, String message) {
        JSONObject errorObject = new JSONObject();
        errorObject.put("error", errorCode);
        errorObject.put("message", message);
        return new JSONArray().put(errorObject);
    }
}