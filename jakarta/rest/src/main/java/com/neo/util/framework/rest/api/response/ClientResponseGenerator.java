package com.neo.util.framework.rest.api.response;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface ClientResponseGenerator {

    /**
     * Parses the errorCode end message to the error entity
     *
     * @param errorCode error code of the error
     * @param message the message
     * @return parsed response
     */
    Response generateErrorResponse(int code, String errorCode, String message);

    /**
     * Get the {@link MediaType} of this generator
     */
    MediaType getMediaType();
}
