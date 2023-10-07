package com.neo.util.framework.api.request;

/**
 * This interfaces defines the capability for auditing request
 */
public interface RequestAuditProvider {

    /**
     * Saves the request event for the given {@link RequestDetails}
     *
     * @param requestDetails that have been executed
     * @param failed if the request failed
     * @param <T> the type of request
     */
    <T extends RequestDetails> void audit(T requestDetails, boolean failed);
}