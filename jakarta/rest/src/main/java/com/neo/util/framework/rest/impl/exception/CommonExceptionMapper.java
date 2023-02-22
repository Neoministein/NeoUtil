package com.neo.util.framework.rest.impl.exception;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.NoContentFoundException;
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
public class CommonExceptionMapper implements ExceptionMapper<CommonRuntimeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonExceptionMapper.class);

    @Inject
    protected ResponseGenerator responseGenerator;

    @Override
    public Response toResponse(CommonRuntimeException ex) {
        LOGGER.error("A CommonRuntimeException occurred during a rest call with id [{}] and message: {}",
                ex.getExceptionId(), ex.getMessage());
        if (ex.getInternal()) {
            return responseGenerator.error(500, RuntimeExceptionMapper.EX_INTERNAL_RUNTIME_EXCEPTION);
        }

        if (ex instanceof NoContentFoundException) {
            return responseGenerator.error(404, ex);
        }

        return responseGenerator.error(400, ex);
    }
}
