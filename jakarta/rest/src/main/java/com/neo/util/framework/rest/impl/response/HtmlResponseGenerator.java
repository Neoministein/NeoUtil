package com.neo.util.framework.rest.impl.response;

import com.neo.util.common.impl.html.HtmlStringTemplate;
import com.neo.util.framework.rest.api.response.ClientResponseGenerator;
import com.neo.util.framework.rest.impl.JaxResourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.MediaType;

import java.util.Optional;

@ApplicationScoped
public class HtmlResponseGenerator implements ClientResponseGenerator {

    protected final JaxResourceUtils jaxResourceUtils;

    @Inject
    public HtmlResponseGenerator(JaxResourceUtils jaxResourceUtils) {
        this.jaxResourceUtils = jaxResourceUtils;
    }

    @Override
    public String parseToErrorEntity(String errorCode, String message) {
        if (displayErrorAsToast()) {
            return "null";
        }
        return HtmlStringTemplate.HTML."""
                <div>
                    <b>Error: </b>\{errorCode}
                </div>
                <div>
                    <b>Message: </b>\{message}
                </div>
                """.content();
    }

    @Override
    public Optional<String> responseToErrorCode(Object entity) {
        if (displayErrorAsToast()) {
            return null;
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
