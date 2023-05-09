package com.neo.util.framework.rest.impl.exception;

import com.neo.util.common.impl.exception.ExceptionDetails;
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
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    public static final ExceptionDetails EX_INTERNAL_RUNTIME_EXCEPTION = new ExceptionDetails(
            "unknown", "Internal server error please try again later", true);

    @Inject
    protected ResponseGenerator responseGenerator;

    @Override
    public Response toResponse(RuntimeException ex) {
        LOGGER.error("A [{}] occurred with message [{}] setting status to [200]",
                ex.getClass().getSimpleName(), ex.getMessage());
        return responseGenerator.error(500, EX_INTERNAL_RUNTIME_EXCEPTION);
    }
}
