package com.neo.util.framework.impl.request;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.request.recording.RequestRecordingManager;
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

    @Inject
    protected RequestRecordingManager requestRecordManager;

    /**
     * Executes the runnable within a {@link RequestScoped} with the provided {@link RequestDetails} and logs the request
     *
     * @param requestDetails the details of the current request
     * @param runnable the code to execute
     */
    public void execute(RequestDetails requestDetails, Runnable runnable) {
        LOGGER.trace("Starting to executing within context, {}", requestDetails);
        RequestContextController requestContextController = requestContextControllerFactory.get();
        requestContextController.activate();

        boolean failed = true;
        try {
            requestDetailsProducer.setRequestDetails(requestDetails);
            runnable.run();
            failed = false;
        } finally {
            requestRecordManager.recordRequest(requestDetails, failed);
            requestContextController.deactivate();
            LOGGER.trace("Finished execution context [{}]", requestDetails.getRequestId());
        }
    }

    /**
     * Executes the checked runnable within a {@link RequestScoped} with the provided {@link RequestDetails} and logs the request
     *
     * @param requestDetails the details of the current request
     * @param runnable the code to execute
     *
     * @throws E might throw this type of error
     */
    public <E extends Exception> void executeChecked(RequestDetails requestDetails, CheckedRunnable<E> runnable) throws E {
        LOGGER.trace("Starting to executing within context, {}", requestDetails);
        RequestContextController requestContextController = requestContextControllerFactory.get();
        requestContextController.activate();

        boolean failed = true;
        try {
            requestDetailsProducer.setRequestDetails(requestDetails);
            runnable.run();
            failed = false;
        } finally {
            requestRecordManager.recordRequest(requestDetails, failed);
            requestContextController.deactivate();
            LOGGER.trace("Finished execution context [{}]", requestDetails.getRequestId());
        }
    }
}
