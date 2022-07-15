package com.neo.util.framework.rest.impl.exception;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.exception.InternalLogicException;
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
public class InternalLogicMapper implements ExceptionMapper<InternalLogicException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalLogicMapper.class);

    protected ObjectNode errorObject;

    @Inject
    protected ResponseGenerator responseGenerator;

    @PostConstruct
    protected void init() {
        errorObject = responseGenerator.errorObject("unknown","Internal server error please try again later");
    }

    @Override
    public Response toResponse(InternalLogicException ex) {
        LOGGER.error("A exception occurred during a rest call", ex);
        return responseGenerator.error(500, errorObject);
    }
}
