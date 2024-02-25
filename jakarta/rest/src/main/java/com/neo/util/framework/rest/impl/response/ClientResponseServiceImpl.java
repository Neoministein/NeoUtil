package com.neo.util.framework.rest.impl.response;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ExceptionDetails;
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
    public Response error(int code, CommonRuntimeException exceptionDetails) {
        ClientResponseGenerator generator = getGenerator(jaxResourceUtils.getCurrentMediaType()).orElse(DEFAULT_GENERATOR);
        return Response
                .status(code)
                .entity(generator.parseToErrorEntity(exceptionDetails.getExceptionId(), exceptionDetails.getMessage()))
                .header(VALID_BACKEND_ERROR, true)
                .build();
    }

    @Override
    public Response error(int code, ExceptionDetails exceptionDetails, Object... arguments) {
        return error(code, new CommonRuntimeException(exceptionDetails, arguments));
    }

    @Override
    public Response error(int code, String errorCode, String message) {
        return error(code, new CommonRuntimeException(new ExceptionDetails(errorCode, message, false)));
    }

    @Override
    public Optional<String> responseToErrorCode(Object entity) {
        return getGenerator(jaxResourceUtils.getCurrentMediaType()).orElse(DEFAULT_GENERATOR)
                .responseToErrorCode(entity);
    }

    @Override
    public Optional<ClientResponseGenerator> getGenerator(MediaType mediaType) {
        return Optional.ofNullable(responseGeneratorMap.get(mediaType.getSubtype()));
    }

    private static final class DefaultClientResponseGenerator implements ClientResponseGenerator {

        @Override
        public String parseToErrorEntity(String errorCode, String message) {
            return "Error: " + errorCode + "\nMessage: " + message;
        }

        @Override
        public Optional<String> responseToErrorCode(Object entity) {
            if (entity instanceof String s) {
                try {
                    return Optional.of(s.substring(6, s.indexOf('\n')));
                } catch (IndexOutOfBoundsException ignored) {}
            }
            return Optional.empty();
        }

        @Override
        public MediaType getMediaType() {
            return null;
        }
    }
}
