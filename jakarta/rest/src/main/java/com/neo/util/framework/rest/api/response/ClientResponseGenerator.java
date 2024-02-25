package com.neo.util.framework.rest.api.response;

import jakarta.ws.rs.core.MediaType;

import java.util.Optional;

public interface ClientResponseGenerator {

    /**
     * Parses the errorCode end message to the error entity
     *
     * @param errorCode error code of the error
     * @param message the message
     * @return parsed response
     */
    String parseToErrorEntity(String errorCode, String message);

    /**
     * Retries to retrieve the error code from the given response object.
     *
     * @param entity the response entity
     * @return the error code
     */
    Optional<String> responseToErrorCode(Object entity);

    /**
     * Get the {@link MediaType} of this generator
     */
    MediaType getMediaType();
}
