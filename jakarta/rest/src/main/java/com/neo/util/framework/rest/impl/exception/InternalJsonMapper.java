package com.neo.util.framework.rest.impl.exception;

import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class InternalJsonMapper implements ExceptionMapper<InternalJsonException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalJsonMapper.class);

    protected static final String E_INVALID_JSON = "json/000";

    @Inject
    protected ResponseGenerator responseGenerator;

    @Override
    public Response toResponse(InternalJsonException ex) {
        LOGGER.warn("Invalid json format in the request body [{}]", ex.getMessage());
        return responseGenerator.error(400, E_INVALID_JSON, "Invalid json format in the request body: " + ex.getMessage());
    }
}
