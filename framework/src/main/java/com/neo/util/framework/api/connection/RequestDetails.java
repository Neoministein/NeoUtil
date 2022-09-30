package com.neo.util.framework.api.connection;

import java.util.Date;

/**
 * This interface consolidates all the data based on this request.
 */
public interface RequestDetails {

    /**
     * Returns the caller of the request
     */
    String getCaller();

    /**
     * A unique identifier for the current request
     */
    String getRequestId();

    /**
     * The current context of the request
     */
    RequestContext getRequestContext();

    /**
     * Returns the date the request has been started
     */
    Date getRequestStartDate();
}
