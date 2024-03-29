package com.neo.util.framework.rest.impl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.parser.OutboundJsonView;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

@Provider
@Priority(Priorities.ENTITY_CODER)
@Produces({MediaType.APPLICATION_JSON, "text/json", "*/*"})
public class GenericJsonWriter implements MessageBodyWriter<Object> {

    private static final String JSON = "json";
    private static final String PLUS_JSON = "+json";

    @Context
    protected ResourceInfo resourceInfo;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return supportsMediaType(mediaType);
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return MessageBodyWriter.super.getSize(o, type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        if (JsonNode.class.isAssignableFrom(type)) {
            entityStream.write(o.toString().getBytes(getCharset(mediaType)));
            return;
        }

        Charset charset = getCharset(mediaType);
        OutboundJsonView anno = resourceInfo.getResourceMethod().getAnnotation(OutboundJsonView.class);
        if (anno == null) {
            entityStream.write(JsonUtil.toJson(o).getBytes(charset));
        } else {
            entityStream.write(JsonUtil.toJson(o, anno.value()).getBytes(charset));
        }
    }

    protected boolean supportsMediaType(final MediaType mediaType) {
        return mediaType.getSubtype().equals(JSON) || mediaType.getSubtype().endsWith(PLUS_JSON);
    }

    protected Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? Charset.defaultCharset() : Charset.forName(name);
    }
}
