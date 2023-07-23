package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.AbstractRequestDetails;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.queue.QueueMessage;

/**
 * This impl consolidates all the data for a single request by a queue.
 */
public class QueueRequestDetails extends AbstractRequestDetails {

    /**
     * The details from the original caller
     */
    protected final long originalRequestId;
    protected final String originalInitiator;
    protected final String originalInstanceId;

    public QueueRequestDetails(String instanceId, QueueMessage queueMessage, RequestContext requestContext) {
        super(instanceId, requestContext);
        this.originalRequestId = queueMessage.getRequestId();
        this.originalInitiator = queueMessage.getInitiator();
        this.originalInstanceId = queueMessage.getInstanceId();
    }

    public QueueRequestDetails(String instanceId, String originalInitiator, long originalRequestId, String originalInstanceId, RequestContext requestContext) {
        super(instanceId, requestContext);
        this.originalRequestId = originalRequestId;
        this.originalInitiator = originalInitiator;
        this.originalInstanceId = originalInstanceId;
    }

    @Override
    public String getInitiator() {
        return originalInitiator;
    }

    public record Context(String queueName) implements RequestContext {
        @Override
        public String type() {
            return "Queue";
        }

        @Override
        public String toString() {
            return queueName;
        }
    }
}
