package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class RequestDetailsProducer {

    protected RequestDetails requestDetails = null;

    @Produces
    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}
