package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.RequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to standardize the execution of code inside a {@link RequestScoped} with the details of the caller.
 */
@ApplicationScoped
public class RequestContextExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContextExecutor.class);

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
        LOGGER.debug("Starting to executing within context, {}", requestDetails);
        RequestContextController requestContextController = requestContextControllerFactory.get();
        requestContextController.activate();
        try {
            requestDetailsProducer.setRequestDetails(requestDetails);
            runnable.run();
        } finally {
            requestContextController.deactivate();
            LOGGER.debug("Finished execution context [{}]", requestDetails.getRequestId());
        }
    }
}
