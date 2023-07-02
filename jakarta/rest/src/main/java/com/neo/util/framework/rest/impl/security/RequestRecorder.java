package com.neo.util.framework.rest.impl.security;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.component.ApplicationComponent;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.rest.percistence.RequestSearchable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
public class RequestRecorder implements ContainerResponseFilter, ApplicationComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRecorder.class);

    public static final String ENABLED_CONFIG = "request-recorder.enabled";

    protected boolean enabled;

    @Inject
    protected jakarta.inject.Provider<RequestDetails> requestDetailsProvider;

    @Inject
    protected SearchProvider searchProvider;

    @Inject
    protected ConfigService configService;

    @Inject
    protected ResponseGenerator responseGenerator;

    @Override
    public boolean enabled() {
        return enabled;
    }

    @PostConstruct
    public void reload() {
        this.enabled = configService.get(ENABLED_CONFIG).asBoolean().orElse(true)
                && searchProvider.enabled();
    }

    @Override
    public void filter(ContainerRequestContext req,
            ContainerResponseContext resp) {
        if (enabled() && resp.getStatus() != 404 && !resp.getStatusInfo().getReasonPhrase().equals("Not Found")) {
            try {
                RequestSearchable searchable = parseRequestSearchable((HttpRequestDetails) requestDetailsProvider.get(), req, resp);
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
                requestDetails.getUser().map(Principal::getName).orElse(StringUtils.EMPTY),
                requestDetails.getRemoteAddress(),
                requestDetails.getRequestContext().toString(),
                Integer.toString(resp.getStatus()),
                parseErrorCodeIfPresent(resp),
                System.currentTimeMillis() - requestDetails.getRequestStartDate().toEpochMilli(),
                Optional.ofNullable(req.getHeaders().get(HttpHeaders.USER_AGENT)).map(Object::toString).orElse(""));
    }

    protected String parseErrorCodeIfPresent(ContainerResponseContext containerResponse) {
        if (containerResponse.getStatus() >= 400) {
            return responseGenerator.responseToErrorCode(containerResponse.getEntity());
        }
        return StringUtils.EMPTY;
    }
}