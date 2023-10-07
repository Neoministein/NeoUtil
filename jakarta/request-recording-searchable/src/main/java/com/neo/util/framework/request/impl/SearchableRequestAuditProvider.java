package com.neo.util.framework.request.impl;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.request.RequestAuditProvider;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.request.api.RequestSearchableParser;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Alternative
@Priority(PriorityConstants.APPLICATION)
@ApplicationScoped
public class SearchableRequestAuditProvider implements RequestAuditProvider {

    @Inject
    protected SearchProvider searchProvider;

    protected Map<Class<? extends RequestDetails>, RequestSearchableParser<? extends RequestDetails>> requestRecorderMap;

    /**
     * Stores all RequestRecorder. This is done only once at startup since it cannot be changed at runtime.
     */
    @Inject
    protected void init(Instance<RequestSearchableParser<?>> requestRecorders) {
        Map<Class<? extends RequestDetails>, RequestSearchableParser<?>> newMap = new HashMap<>();
        for (RequestSearchableParser<?> requestRecorder: requestRecorders) {
            newMap.put(requestRecorder.getRequestType(), requestRecorder);
        }
        requestRecorderMap = Collections.unmodifiableMap(newMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RequestDetails> void audit(T requestDetails, boolean failed) {
        RequestSearchableParser<T> searchableParser = (RequestSearchableParser<T>) requestRecorderMap.get(requestDetails.getClass());

        if(searchableParser.enabled() && searchProvider.enabled()) {
            searchProvider.index(searchableParser.parse(requestDetails, failed));
        }
    }
}
