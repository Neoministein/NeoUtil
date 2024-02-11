package com.neo.util.framework.rest.impl.parser;

import com.neo.util.common.impl.html.HtmlElement;
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
@Produces({MediaType.TEXT_HTML, "*/*"})
public class GenericHtmlWriter implements MessageBodyWriter<HtmlElement> {

    @Context
    protected ResourceInfo resourceInfo;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return supportsMediaType(mediaType);
    }

    @Override
    public long getSize(HtmlElement o, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return MessageBodyWriter.super.getSize(o, type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(HtmlElement o, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        entityStream.write(o.content().getBytes(getCharset(mediaType)));
    }

    protected boolean supportsMediaType(final MediaType mediaType) {
        return mediaType.getSubtype().equals(MediaType.TEXT_HTML_TYPE.getSubtype());
    }

    protected Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? Charset.defaultCharset() : Charset.forName(name);
    }
}
