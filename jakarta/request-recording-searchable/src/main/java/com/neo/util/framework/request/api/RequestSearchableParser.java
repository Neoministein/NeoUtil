package com.neo.util.framework.request.api;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.request.percistence.RequestLogSearchable;

/**
 * Parses the {@link RequestDetails} to {@link com.neo.util.framework.api.persistence.search.Searchable}
 *
 * @param <T> type of {@link RequestDetails}
 */
public interface RequestSearchableParser<T extends RequestDetails> {

    /**
     * If the parser is enabled
     */
    boolean enabled();

    /**
     * Parses the request details
     *
     * @param requestDetails the request details
     * @param failed if the request failed
     *
     * @return parsed searchable
     */
    RequestLogSearchable parse(T requestDetails, boolean failed);

    /**
     * The type
     */
    Class<T> getRequestType();
}
