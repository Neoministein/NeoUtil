package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.AbstractRequestDetails;
import com.neo.util.framework.api.request.RequestContext;

/**
 * This impl consolidates all the data for a single request by a timer.
 */
public class SchedulerRequestDetails extends AbstractRequestDetails {

    public SchedulerRequestDetails(String requestId, RequestContext requestContext) {
        super(requestId, requestContext);
    }

    @Override
    public String getInitiator() {
        return requestContext.toString();
    }

    record Context(String scheduleName) implements RequestContext {
        @Override
        public String type() {
            return "Scheduler";
        }

        @Override
        public String toString() {
            return scheduleName;
        }
    }
}
