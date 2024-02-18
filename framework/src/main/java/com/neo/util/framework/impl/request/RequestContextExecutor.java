package com.neo.util.framework.impl.request;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.framework.api.request.RequestAuditProvider;
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

    protected final Provider<RequestContextController> requestContextControllerFactory;
    protected final RequestDetailsProducer requestDetailsProducer;
    protected final RequestAuditProvider requestAuditProvider;


    @Inject
    public RequestContextExecutor(Provider<RequestContextController> requestContextControllerFactory,
                                  RequestDetailsProducer requestDetailsProducer, RequestAuditProvider requestAuditProvider) {
        this.requestContextControllerFactory = requestContextControllerFactory;
        this.requestDetailsProducer = requestDetailsProducer;
        this.requestAuditProvider = requestAuditProvider;
    }

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
            requestAuditProvider.audit(requestDetails, failed);
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
            requestAuditProvider.audit(requestDetails, failed);
            requestContextController.deactivate();
            LOGGER.trace("Finished execution context [{}]", requestDetails.getRequestId());
        }
    }
}
