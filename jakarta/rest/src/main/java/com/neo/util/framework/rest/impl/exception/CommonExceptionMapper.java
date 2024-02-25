package com.neo.util.framework.rest.impl.exception;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@ApplicationScoped
public class CommonExceptionMapper implements ExceptionMapper<CommonRuntimeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonExceptionMapper.class);

    @Inject
    protected ClientResponseService clientResponseService;

    @Override
    public Response toResponse(CommonRuntimeException ex) {
        if (ex.getInternal()) {
            LOGGER.error("A [{}] occurred with message [{}] setting status to [500]",
                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
            return clientResponseService.error(500, RuntimeExceptionMapper.EX_INTERNAL_RUNTIME_EXCEPTION);
        }

        if (ex instanceof NoContentFoundException) {
            LOGGER.warn("A [{}] occurred with message [{}] setting status to [404]",
                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
            return clientResponseService.error(404, ex);
        }

        LOGGER.warn("A [{}] occurred with message [{}] setting status to [400]",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return clientResponseService.error(400, ex);
    }
}
