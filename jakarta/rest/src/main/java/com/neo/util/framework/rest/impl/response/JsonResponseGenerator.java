package com.neo.util.framework.rest.impl.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.response.ClientResponseGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;

import java.util.Optional;

@ApplicationScoped
public class JsonResponseGenerator implements ClientResponseGenerator {

    @Override
    public String parseToErrorEntity(String errorCode, String message) {
        ObjectNode errorObject = JsonUtil.emptyObjectNode();
        errorObject.put("code", errorCode);
        errorObject.put("message", message);
        return errorObject.toString();
    }

    @Override
    public Optional<String> responseToErrorCode(Object entity) {
        if (entity instanceof String responseString) {
            try {
                JsonNode responseBody = JsonUtil.fromJson(responseString);
                if (responseBody.has("code")) {
                    return Optional.of(responseBody.get("code").asText());
                }
            } catch (ValidationException ignored) {}

        }

        return Optional.empty();
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }
}
