package com.neo.util.framework.api.request.recording;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.percistence.request.RequestLogSearchable;

public interface RequestRecorder<T extends RequestDetails> {

    RequestLogSearchable parseToSearchable(T requestDetails, boolean failed);

    boolean enabled();

    Class<T> getRequestType();
}
