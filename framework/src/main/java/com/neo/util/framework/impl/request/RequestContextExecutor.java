package com.neo.util.framework.impl.request;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.framework.api.request.RequestAuditProvider;
import com.neo.util.framework.api.request.RequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

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
     * Helper for {@link #execute(RequestDetails, Runnable, boolean)}
     */
    public void execute(RequestDetails requestDetails, Runnable runnable) {
        this.execute(requestDetails, runnable, false);
    }

    /**
     * Executes the runnable within a {@link RequestScoped} with the provided {@link RequestDetails} and logs the request
     *
     * @param requestDetails the details of the current request
     * @param runnable the code to execute
     * @param forceOverrideDetails if the request details should be overridden even if there's already an active request
     */
    public void execute(RequestDetails requestDetails, Runnable runnable, boolean forceOverrideDetails) {
        LOGGER.trace("Starting to executing within context, {}", requestDetails);
        RequestContextController requestContextController = requestContextControllerFactory.get();
        boolean wasActivated = requestContextController.activate();

        boolean failed = true;
        try {
            if (wasActivated || forceOverrideDetails) {
                requestDetailsProducer.setRequestDetails(requestDetails);
            }
            runnable.run();
            failed = false;
        } finally {
            requestAuditProvider.audit(requestDetails, failed);
            if (wasActivated) {
                requestContextController.deactivate();
            }
            LOGGER.trace("Finished execution context [{}]", requestDetails.getRequestId());
        }
    }

    /**
     * Helper for {@link #executeChecked(RequestDetails, CheckedRunnable, boolean)}
     */
    public <E extends Exception> void executeChecked(RequestDetails requestDetails, CheckedRunnable<E> runnable) throws E {
        executeChecked(requestDetails, runnable, false);
    }

    /**
     * Executes the checked runnable within a {@link RequestScoped} with the provided {@link RequestDetails} and logs the request
     *
     * @param requestDetails the details of the current request
     * @param runnable the code to execute
     * @param forceOverrideDetails if the request details should be overridden even if there's already an active request
     * @throws E might throw this type of error
     */
    public <E extends Exception> void executeChecked(RequestDetails requestDetails, CheckedRunnable<E> runnable, boolean forceOverrideDetails) throws E {
        LOGGER.trace("Starting to executing within context, {}", requestDetails);
        RequestContextController requestContextController = requestContextControllerFactory.get();
        boolean wasActivated = requestContextController.activate();

        boolean failed = true;
        try {
            if (wasActivated || forceOverrideDetails) {
                requestDetailsProducer.setRequestDetails(requestDetails);
            }
            runnable.run();
            failed = false;
        } finally {
            requestAuditProvider.audit(requestDetails, failed);
            if (wasActivated) {
                requestContextController.deactivate();
            }
            LOGGER.trace("Finished execution context [{}]", requestDetails.getRequestId());
        }
    }

    /**
     * Executes the runnable within a {@link RequestScoped} based on the given
     * {@link ExecutorService} with the provided {@link RequestDetails} and logs the
     * request.
     *
     * @param executor to create the new thread from
     * @param semaphore used to restrict how many can run in parallel
     * @param runnable the task to execute
     *
     * @return completable future of the task
     */
    public CompletableFuture<Void> executeAsync(ExecutorService executor, Semaphore semaphore, Runnable runnable) {
        RequestDetails requestDetails = requestDetailsProducer.getRequestDetails();
        return CompletableFuture.runAsync(() -> {
            requestDetails.applyLogContext();
            ThreadUtils.rethrowInterrupt(semaphore::acquire);
            try {
                execute(requestDetails, runnable);
            } finally {
                semaphore.release();
            }
        }, executor);
    }
}
