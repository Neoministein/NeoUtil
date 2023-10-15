package com.neo.util.framework.request.impl.recorders;

import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;
import com.neo.util.framework.request.percistence.RequestLogSearchable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QueueRequestRecorder extends AbstractRequestRecorder<QueueRequestDetails> {

    @Override
    public RequestLogSearchable parse(QueueRequestDetails requestDetails, boolean failed) {
        return new RequestLogSearchable(requestDetails, failed);
    }

    @Override
    public Class<QueueRequestDetails> getRequestType() {
        return QueueRequestDetails.class;
    }

    @ApplicationScoped
    public static class SchedulerRequestRecorder extends AbstractRequestRecorder<SchedulerRequestDetails> {

        @Override
        public RequestLogSearchable parse(SchedulerRequestDetails requestDetails, boolean failed) {
            return new RequestLogSearchable(requestDetails, failed);
        }

        @Override
        public Class<SchedulerRequestDetails> getRequestType() {
            return SchedulerRequestDetails.class;
        }
    }
}
