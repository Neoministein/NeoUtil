package com.neo.util.framework.rest.impl.response;

import com.neo.util.common.impl.html.HtmlStringTemplate;
import com.neo.util.framework.rest.api.response.ClientResponseGenerator;
import com.neo.util.framework.rest.impl.JaxResourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@ApplicationScoped
public class HtmlResponseGenerator implements ClientResponseGenerator {

    protected final JaxResourceUtils jaxResourceUtils;

    @Inject
    public HtmlResponseGenerator(JaxResourceUtils jaxResourceUtils) {
        this.jaxResourceUtils = jaxResourceUtils;
    }

    @Override
    public Response generateErrorResponse(int code, String errorCode, String message) {
        Response.ResponseBuilder response = Response.status(code);

        if (displayErrorAsToast()) {
            response.header("HX-Retarget", "");
            response.header("HX-Reswap", "");
            response.entity("null");
        }
        response.entity(HtmlStringTemplate.HTML."""
                <div>
                    <b>Error: </b>\{errorCode}
                </div>
                <div>
                    <b>Message: </b>\{message}
                </div>
                """);

        response.header("HX-Location", "/error");
        return response.build();
    }

    @Override
    public Optional<String> responseToErrorCode(Object entity) {
        if (displayErrorAsToast()) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.TEXT_HTML_TYPE;
    }

    protected boolean displayErrorAsToast() {
        return jaxResourceUtils.getAnnotation(GET.class).isEmpty();
    }
}
