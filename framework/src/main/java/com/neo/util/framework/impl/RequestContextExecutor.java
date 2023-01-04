package com.neo.util.framework.impl;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * A utility class to standardize the execution of code inside a {@link RequestScoped} with the details of the caller.
 */
@ApplicationScoped
public class RequestContextExecutor {

    @Inject
    protected Provider<RequestContextController> requestContextControllerFactory;

    @Inject
    protected RequestDetailsProducer requestDetailsProducer;

    /**
     * Executes the runnable within a {@link RequestScoped} with the provided {@link RequestDetails}
     *
     * @param requestDetails the details of the current request
     * @param runnable the code to execute
     */
    public void execute(RequestDetails requestDetails, Runnable runnable) {
        RequestContextController requestContextController = requestContextControllerFactory.get();
        requestContextController.activate();
        try {
            requestDetailsProducer.setRequestDetails(requestDetails);
            runnable.run();
        } finally {
            requestContextController.deactivate();
        }
    }
}
