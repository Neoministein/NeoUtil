package com.neo.util.framework.rest.impl.response;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.api.request.UserRequest;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.rest.api.response.ClientResponseGenerator;
import com.neo.util.framework.rest.impl.JaxResourceUtils;
import com.neo.util.framework.rest.web.htmx.ResourceFormattingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;
import static com.neo.util.framework.rest.api.response.ClientResponseService.VALID_BACKEND_ERROR;

@ApplicationScoped
public class HtmlResponseGenerator implements ClientResponseGenerator {


    protected final JaxResourceUtils jaxResourceUtils;
    protected final ResourceFormattingService formattingService;

    protected final Provider<UserRequestDetails> detailsProvider;

    @Inject
    public HtmlResponseGenerator(JaxResourceUtils jaxResourceUtils, ResourceFormattingService resourceFormattingService,
                                 @UserRequest Provider<UserRequestDetails> detailsProvider) {
        this.jaxResourceUtils = jaxResourceUtils;
        this.formattingService = resourceFormattingService;
        this.detailsProvider = detailsProvider;
    }

    @Override
    public Response generateErrorResponse(int code, String errorCode, String message) {
        Response.ResponseBuilder response = Response.status(code);
        response.header(VALID_BACKEND_ERROR, errorCode);
        response.header("HX-Retarget", "#toastAnchor");
        response.header("HX-Reswap", "afterend");

        response.entity(generateToastElement(code, errorCode, message));
        return response.build();
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.TEXT_HTML_TYPE;
    }

    protected HtmlElement generateToastElement(int code, String errorCode, String message) {
        String id = UUID.randomUUID().toString();
        UserRequestDetails requestDetails = detailsProvider.get();
        return HTML."""
                        <div id="toast-\{id}" class="toast show " role="alert" aria-live="assertive" aria-atomic="true">
                            <div class="toast-header text-bg-danger">
                                <strong class="me-auto">Error: \{code}</strong>
                                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                            </div>
                            <div class="accordion accordion-flush text-bg-danger" id="accordionFlushExample">
                                <div class="accordion-item">
                                    <h5 class="accordion-header">
                                        <button class="accordion-button collapsed" style="font-site: 15px;" type="button" data-bs-toggle="collapse" data-bs-target="#collapse-\{id}" aria-expanded="false" aria-controls="collapse-\{id}">
                                        \{message}
                                        </button>
                                    </h5>
                                    <div id="collapse-\{id}" class="accordion-collapse collapse" data-bs-parent="#accordionFlushExample">
                                        <div class="accordion-body">
                                            <strong class="me-auto">ErrorCode: </strong> \{errorCode} <br>
                                            <strong class="me-auto">Context: </strong> \{requestDetails.getRequestContext()} <br>
                                            <strong class="me-auto">Time: </strong> \{formattingService.toDateSecond(requestDetails.getRequestStartDate())} <br>
                                            <strong class="me-auto">RequestId: </strong> \{requestDetails.getRequestId()} <br>
                                            <strong class="me-auto">Trace: </strong> \{requestDetails.getTraceId()} <br>
                                        </div>
                                    </div>
                                </div>
                            <div>
                        </div>
                    """;
    }
}
