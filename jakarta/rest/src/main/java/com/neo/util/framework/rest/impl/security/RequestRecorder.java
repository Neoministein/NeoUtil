package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.component.ApplicationComponent;
import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.component.ApplicationComponentManager;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.rest.percistence.RequestSearchable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

@Provider
@RequestScoped
public class RequestRecorder implements ContainerResponseFilter, ApplicationComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRecorder.class);

    public static final String FRAMEWORK_PROVIDED_ERROR = "FRAMEWORK_PROVIDED_ERROR";

    @Inject
    protected RequestDetails requestDetails;

    @Inject
    protected SearchProvider searchProvider;

    @Inject
    protected ApplicationComponentManager applicationComponentManager;

    @Inject
    protected ResponseGenerator responseGenerator;

    public boolean enabled() {
        return applicationComponentManager.isComponentEnabled(RequestRecorder.class.getSimpleName()).orElse(true)
                && searchProvider.enabled();
    }

    @Override
    public void filter(ContainerRequestContext req,
            ContainerResponseContext resp) {
        if (enabled()) {
            try {
                RequestSearchable searchable = parseRequestSearchable((HttpRequestDetails) requestDetails, req, resp);
                searchProvider.index(searchable);
                LOGGER.trace("Status [{}] Context: [{}] Took: [{}]", resp.getStatus(), searchable.getContext(), searchable.getProcessTime());
            } catch (Exception ex) {
                LOGGER.warn("Unable to parse request segments [{}]", ex.getMessage());
            }
        }
    }

    protected RequestSearchable parseRequestSearchable(HttpRequestDetails requestDetails, ContainerRequestContext req,
                                                       ContainerResponseContext resp) {
        return new RequestSearchable(
                Instant.now(),
                requestDetails.getRequestId(),
                requestDetails.getUser().map(Principal::getName).orElse(null),
                requestDetails.getRemoteAddress(),
                requestDetails.getRequestContext().toString(),
                Integer.toString(resp.getStatus()),
                parseErrorCodeIfPresent(resp),
                System.currentTimeMillis() - requestDetails.getRequestStartDate().toEpochMilli(),
                Optional.ofNullable(req.getHeaders().get(HttpHeaders.USER_AGENT)).map(Object::toString).orElse(null));
    }

    protected String parseErrorCodeIfPresent(ContainerResponseContext containerResponse) {
        if (containerResponse.getStatus() >= 400) {
            if (containerResponse.getHeaders().containsKey(ResponseGenerator.VALID_BACKEND_ERROR)) {
                return responseGenerator.responseToErrorCode(containerResponse.getEntity());
            }
            return FRAMEWORK_PROVIDED_ERROR;
        }
        return null;
    }
}