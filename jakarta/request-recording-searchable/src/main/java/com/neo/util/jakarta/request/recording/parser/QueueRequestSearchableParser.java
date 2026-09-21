package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.api.queue.QueueRequestDetails;
import com.neo.util.jakarta.request.recording.searchable.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QueueRequestSearchableParser extends AbstractRequestSearchableParser<QueueRequestDetails> {

    @Override
    public RequestLogSearchable parse(QueueRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<QueueRequestDetails> getRequestType() {
        return QueueRequestDetails.class;
    }
}
