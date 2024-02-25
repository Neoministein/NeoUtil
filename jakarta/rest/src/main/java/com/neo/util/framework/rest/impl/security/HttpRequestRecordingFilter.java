package com.neo.util.framework.rest.impl.security;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.request.RequestAuditProvider;
import com.neo.util.framework.api.request.UserRequest;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@RequestScoped
public class HttpRequestRecordingFilter implements ContainerResponseFilter {

    public static final String FRAMEWORK_PROVIDED_ERROR = "FRAMEWORK_PROVIDED_ERROR";

    @Inject
    @UserRequest
    protected UserRequestDetails userRequestDetails;

    @Inject
    protected RequestAuditProvider requestAuditProvider;

    @Inject
    protected ClientResponseService clientResponseService;


    @Override
    public void filter(ContainerRequestContext req,
            ContainerResponseContext resp) {
        if (userRequestDetails instanceof HttpRequestDetails httpRequestDetails) {
            String error = parseErrorCodeIfPresent(resp);
            httpRequestDetails.setStatus(resp.getStatus());
            httpRequestDetails.setError(error);
            requestAuditProvider.audit(httpRequestDetails, error != null);
        }
    }

    protected String parseErrorCodeIfPresent(ContainerResponseContext containerResponse) {
        if (containerResponse.getStatus() >= 400) {
            if (containerResponse.getHeaders().containsKey(ClientResponseService.VALID_BACKEND_ERROR)) {
                return clientResponseService.responseToErrorCode(containerResponse.getEntity()).orElse(StringUtils.EMPTY);
            }
            return FRAMEWORK_PROVIDED_ERROR;
        }
        return null;
    }
}