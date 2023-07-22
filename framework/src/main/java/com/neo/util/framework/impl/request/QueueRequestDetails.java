package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.AbstractRequestDetails;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.queue.QueueMessage;

/**
 * This impl consolidates all the data for a single request by a queue.
 */
public class QueueRequestDetails extends AbstractRequestDetails {

    /**
     * The caller of the queue
     */
    protected final String originalCaller;

    public QueueRequestDetails(QueueMessage queueMessage, RequestContext requestContext) {
        super(queueMessage.getRequestId(), requestContext);
        this.originalCaller = queueMessage.getCaller();
    }

    public QueueRequestDetails(String originalCaller, String requestId, RequestContext requestContext) {
        super(requestId, requestContext);
        this.originalCaller = originalCaller;
    }

    @Override
    public String getInitiator() {
        return originalCaller;
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
