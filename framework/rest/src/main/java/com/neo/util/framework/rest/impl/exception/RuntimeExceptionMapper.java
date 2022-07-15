package com.neo.util.framework.rest.impl.exception;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    protected ObjectNode errorObject;

    @Inject
    protected ResponseGenerator responseGenerator;

    @PostConstruct
    protected void init() {
        responseGenerator.errorObject("unknown","Internal server error please try again later");
    }

    @Override
    public Response toResponse(RuntimeException ex) {
        LOGGER.error("A unexpected exception occurred during a rest call", ex);
        return responseGenerator.error(500, errorObject);
    }
}
