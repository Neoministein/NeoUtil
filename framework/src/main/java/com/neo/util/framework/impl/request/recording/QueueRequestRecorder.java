package com.neo.util.framework.impl.request.recording;

import com.neo.util.framework.api.request.recording.AbstractRequestRecorder;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.percistence.request.RequestSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QueueRequestRecorder extends AbstractRequestRecorder<QueueRequestDetails> {

    @Override
    public RequestSearchable parseToSearchable(QueueRequestDetails requestDetails, boolean failed) {
        return new RequestSearchable(requestDetails, failed);
    }

    @Override
    public Class<QueueRequestDetails> getRequestType() {
        return QueueRequestDetails.class;
    }
}
