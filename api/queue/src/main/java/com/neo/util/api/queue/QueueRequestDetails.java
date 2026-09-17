package com.neo.util.api.queue;

import com.neo.util.api.request.AbstractRequestDetails;
import com.neo.util.api.request.RequestContext;

/**
 * This impl consolidates all the data for a single request by a queue.
 */
public class QueueRequestDetails extends AbstractRequestDetails {

    /**
     * The details from the original caller
     */
    protected final String originalInitiator;

    public QueueRequestDetails(String instanceId, QueueMessage queueMessage, RequestContext requestContext) {
        super(queueMessage.getTraceId(), instanceId, requestContext);
        this.originalInitiator = queueMessage.getInitiator();
    }

    public QueueRequestDetails(String traceId, String instanceId, String originalInitiator, RequestContext requestContext) {
        super(traceId, instanceId, requestContext);
        this.originalInitiator = originalInitiator;
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
