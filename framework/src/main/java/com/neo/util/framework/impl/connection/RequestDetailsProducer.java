package com.neo.util.framework.impl.connection;

import com.neo.util.framework.api.connection.HttpDetails;
import com.neo.util.framework.api.connection.HttpRequestDetails;
import com.neo.util.framework.api.connection.RequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class RequestDetailsProducer {

    protected RequestDetails requestDetails = null;

    @Produces
    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    @Produces @HttpDetails
    public HttpRequestDetails getHttpRequestDetails() {
        return (HttpRequestDetails) requestDetails;
    }




    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}
