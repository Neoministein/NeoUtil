package com.neo.util.framework.rest.impl.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.response.ClientResponseGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.neo.util.framework.rest.api.response.ClientResponseService.VALID_BACKEND_ERROR;

@ApplicationScoped
public class JsonResponseGenerator implements ClientResponseGenerator {

    @Override
    public Response generateErrorResponse(int code, String errorCode, String message) {
        return Response.status(code)
                .entity(parseToErrorEntity(errorCode, message))
                .header(VALID_BACKEND_ERROR, errorCode)
                .build();
    }

    protected String parseToErrorEntity(String errorCode, String message) {
        ObjectNode errorObject = JsonUtil.emptyObjectNode();
        errorObject.put("code", errorCode);
        errorObject.put("message", message);
        return errorObject.toString();
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }
}
