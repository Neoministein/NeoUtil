package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.api.persistence.search.Searchable;
import com.neo.util.api.request.RequestDetails;
import com.neo.util.jakarta.request.recording.searchable.RequestLogSearchable;

/**
 * Parses the {@link RequestDetails} to {@link Searchable}
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
