package com.neo.util.jakarta.request;

import com.neo.util.api.request.RequestDetails;
import com.neo.util.api.request.UserRequestDetails;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class RequestDetailsProducer {

    protected RequestDetails requestDetails = null;

    @Produces
    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    @Produces
    @UserRequest
    public UserRequestDetails getUserRequestDetails() {
        if (requestDetails instanceof UserRequestDetails userRequestDetails) {
            return userRequestDetails;
        }
        return null;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        if (requestDetails != null) {
            this.requestDetails = requestDetails;
        }
    }
}
