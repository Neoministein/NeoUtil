package com.neo.util.framework.rest.impl.response;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.common.impl.exception.InternalRuntimeException;
import com.neo.util.framework.rest.api.response.ClientResponseGenerator;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import com.neo.util.framework.rest.impl.JaxResourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ClientResponseServiceImpl implements ClientResponseService {

    protected static final ClientResponseGenerator DEFAULT_GENERATOR = new DefaultClientResponseGenerator();

    protected final Map<String, ClientResponseGenerator> responseGeneratorMap = new ConcurrentHashMap<>();

    protected final JaxResourceUtils jaxResourceUtils;

    @Inject
    public ClientResponseServiceImpl(JaxResourceUtils jaxResourceUtils, Instance<ClientResponseGenerator> instance) {
        this.jaxResourceUtils = jaxResourceUtils;
        for (ClientResponseGenerator generator: instance) {
            responseGeneratorMap.put(generator.getMediaType().getSubtype(), generator);
        }
    }

    @Override
    public Response error(int code, ExternalRuntimeException runtimeException) {
        return getGenerator(jaxResourceUtils.getCurrentMediaType()).orElse(DEFAULT_GENERATOR)
                .generateErrorResponse(code, runtimeException.getExceptionId(), runtimeException.getMessage());
    }

    @Override
    public Response error(int code, InternalRuntimeException exceptionDetails) {
        return getGenerator(jaxResourceUtils.getCurrentMediaType()).orElse(DEFAULT_GENERATOR)
                .generateErrorResponse(code, exceptionDetails.getExceptionId(), exceptionDetails.getMessage());
    }

    @Override
    public Response error(int code, ExceptionDetails exceptionDetails, Object... arguments) {
        return error(code, new InternalRuntimeException(exceptionDetails, arguments));
    }

    @Override
    public Response error(int code, String errorCode, String message) {
        return error(code, new InternalRuntimeException(new ExceptionDetails(errorCode, message)));
    }

    @Override
    public Optional<ClientResponseGenerator> getGenerator(MediaType mediaType) {
        return Optional.ofNullable(responseGeneratorMap.get(mediaType.getSubtype()));
    }

    private static final class DefaultClientResponseGenerator implements ClientResponseGenerator {

        @Override
        public Response generateErrorResponse(int code, String errorCode, String message) {
            return Response
                    .status(code)
                    .entity("Error: " + errorCode + "\nMessage: " + message)
                    .build();
        }

        @Override
        public MediaType getMediaType() {
            return null;
        }
    }
}
