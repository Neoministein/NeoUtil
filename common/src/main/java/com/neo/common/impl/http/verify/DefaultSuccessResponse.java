package com.neo.common.impl.http.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.common.api.http.verify.ResponseFormatVerification;
import com.neo.common.impl.exception.InternalJsonException;
import com.neo.common.impl.json.JsonSchemaUtil;
import com.neo.common.impl.json.JsonUtil;
import com.networknt.schema.JsonSchema;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSuccessResponse implements ResponseFormatVerification {

    private static final Logger LOGGER =  LoggerFactory.getLogger(DefaultSuccessResponse.class);

    protected static final JsonSchema JSON_SCHEMA = JsonSchemaUtil.generateSchemaFromResource("schemas/DefaultInternalResponse.json");

    @Override
    @SuppressWarnings("java:S2629")
    public boolean verify(String message) {
        try {
            JsonNode jsonNode = JsonUtil.fromJson(message);
            if (JsonSchemaUtil.isValid(jsonNode,JSON_SCHEMA).isEmpty()) {
                if (jsonNode.get("status").asInt() == 200) {
                    return true;
                }
                LOGGER.warn("Received response which status code {}", jsonNode.get("status").asInt());

                if (jsonNode.has("error")) {
                    JsonNode error = jsonNode.get("error");
                    LOGGER.trace("Error code {} message {}", error.get("code").asText(), error.get("message").asText());
                }
            }
        } catch (InternalJsonException ex) {
            LOGGER.trace("The provided message cannot correctly parsed to json");
        }
        return false;
    }
}
