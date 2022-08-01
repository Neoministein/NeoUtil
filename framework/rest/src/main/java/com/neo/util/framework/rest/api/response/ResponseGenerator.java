package com.neo.util.framework.rest.api.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.ws.rs.core.Response;

/**
 * This interface defines common response which can look different based on applications
 */
public interface ResponseGenerator {

    Response success();

    Response success(JsonNode data);

    Response partialSuccess(int code, JsonNode data, ObjectNode error);

    Response error(int code, String errorCode, String message);

    Response error(int code, ObjectNode error);

    ObjectNode errorObject(String errorCode, Exception ex);

    ObjectNode errorObject(String errorCode, String message);
}
