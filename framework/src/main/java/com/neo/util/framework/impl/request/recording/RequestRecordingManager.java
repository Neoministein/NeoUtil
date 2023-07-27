package com.neo.util.framework.impl.request.recording;

import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.request.recording.RequestRecorder;
import com.neo.util.framework.percistence.request.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class RequestRecordingManager {

    @Inject
    protected SearchProvider searchProvider;

    protected Map<Class<? extends RequestDetails>, RequestRecorder<? extends RequestDetails>> requestRecorderMap;

    /**
     * Stores all RequestRecorder. This is done only once at startup since it cannot be changed at runtime.
     */
    @Inject
    protected void init(Instance<RequestRecorder<?>> requestRecorders) {
        Map<Class<? extends RequestDetails>, RequestRecorder<?>> newMap = new HashMap<>();
        for (RequestRecorder<?> requestRecorder: requestRecorders) {
            newMap.put(requestRecorder.getRequestType(), requestRecorder);
        }
        requestRecorderMap = Collections.unmodifiableMap(newMap);
    }

    public <T extends RequestDetails> void recordRequest(T requestDetails, boolean failed) {
        RequestRecorder<T> requestRecorder = (RequestRecorder<T>) requestRecorderMap.get(requestDetails.getClass());

        if(requestRecorder.enabled() && searchProvider.enabled()) {
            searchProvider.index(requestRecorder.parseToSearchable(requestDetails, failed));
        }
    }

    public void recordSearchable(RequestLogSearchable requestSearchable, Class<? extends RequestDetails> recorderClazz) {
        RequestRecorder<?> requestRecorder = requestRecorderMap.get(recorderClazz);

        if(requestRecorder.enabled()) {
            searchProvider.index(requestSearchable);
        }
    }
}
