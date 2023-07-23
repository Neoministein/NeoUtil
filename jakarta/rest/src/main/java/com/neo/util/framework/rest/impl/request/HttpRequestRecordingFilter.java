package com.neo.util.framework.rest.impl.request;

import com.neo.util.framework.api.request.UserRequest;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.impl.request.recording.RequestRecordingManager;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import com.neo.util.framework.rest.percistence.HttpRequestSearchable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

@Provider
@RequestScoped
public class HttpRequestRecordingFilter implements ContainerResponseFilter {

    public static final String FRAMEWORK_PROVIDED_ERROR = "FRAMEWORK_PROVIDED_ERROR";

    @Inject
    @UserRequest
    protected UserRequestDetails userRequestDetails;

    @Inject
    protected RequestRecordingManager requestRecordingManager;

    @Inject
    protected ResponseGenerator responseGenerator;


    @Override
    public void filter(ContainerRequestContext req,
            ContainerResponseContext resp) {
        if (userRequestDetails != null) {
            HttpRequestSearchable searchable = parseRequestSearchable((HttpRequestDetails) userRequestDetails, req, resp);
            requestRecordingManager.recordSearchable(searchable, HttpRequestDetails.class);
        }
    }

    protected HttpRequestSearchable parseRequestSearchable(HttpRequestDetails requestDetails, ContainerRequestContext req,
                                                           ContainerResponseContext resp) {
        return new HttpRequestSearchable(
                requestDetails,
                resp.getStatus(),
                Optional.ofNullable(req.getHeaders().get(HttpHeaders.USER_AGENT)).map(Object::toString).orElse(null),
                parseErrorCodeIfPresent(resp));
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