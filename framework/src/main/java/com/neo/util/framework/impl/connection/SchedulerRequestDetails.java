package com.neo.util.framework.impl.connection;

import com.neo.util.framework.api.connection.AbstractRequestDetails;
import com.neo.util.framework.api.connection.RequestContext;

/**
 * This impl consolidates all the data for a single request by a timer.
 */
public class SchedulerRequestDetails extends AbstractRequestDetails {

    public SchedulerRequestDetails(String requestId, RequestContext requestContext) {
        super(requestId, requestContext);
    }

    @Override
    public String getCaller() {
        return requestContext.toString();
    }
}
