package com.neo.util.framework.rest.impl.exception;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client error mapper returns the provided response by the {@link ClientErrorException}
 *
 * This is used for 404 errors or invalid HTTP method
 */
@Provider
@ApplicationScoped
public class ClientErrorMapper implements ExceptionMapper<ClientErrorException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientErrorMapper.class);

    @Context
    protected UriInfo uriInfo;

    @Override
    public Response toResponse(ClientErrorException ex) {
        if (ex instanceof NotFoundException) {
            LOGGER.warn("A [{}] occurred with message [{}] setting status to [{}]",
                    ex.getClass().getSimpleName(), uriInfo.getPath(), ex.getResponse().getStatus());
        } else {
            LOGGER.warn("A [{}] occurred with message [{}] setting status to [{}]",
                    ex.getClass().getSimpleName(), ex.getMessage(), ex.getResponse().getStatus());
        }

        return ex.getResponse();
    }
}
