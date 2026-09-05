package com.neo.util.framework.rest.impl.parser;

import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.common.impl.json.JsonUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NoContentException;
import jakarta.ws.rs.ext.Provider;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.jakarta.rs.json.JacksonJsonProvider;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Overridden to supply our down
 * {@link JsonMapper}
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces({MediaType.APPLICATION_JSON, "text/json", MediaType.WILDCARD})
public class CustomJacksonJsonProvider extends JacksonJsonProvider {

    public CustomJacksonJsonProvider() {
        super(JsonUtil.createMapper());
    }

    public CustomJacksonJsonProvider(JsonMapper mapper) {
        super(mapper);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws JacksonException, NoContentException {
        try {
            return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
        } catch (JacksonException ex) {
            throw new ExternalRuntimeException(JsonUtil.EX_INTERNAL_JSON_EXCEPTION, ex.getOriginalMessage());
        }
    }
}