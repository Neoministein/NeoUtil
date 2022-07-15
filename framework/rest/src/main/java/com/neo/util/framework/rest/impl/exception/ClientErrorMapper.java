package com.neo.util.framework.rest.impl.exception;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * The client error mapper returns the provided response by the {@link ClientErrorException}
 *
 * This is used for 404 errors or invalid HTTP method
 */
@Provider
@ApplicationScoped
public class ClientErrorMapper implements ExceptionMapper<ClientErrorException> {

    @Override
    public Response toResponse(ClientErrorException ex) {
        return ex.getResponse();
    }
}
