package com.neo.util.framework.startup.impl.event;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.event.ApplicationShutdownEvent;
import com.neo.util.framework.startup.impl.ListenerSequenceRecorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@ApplicationScoped
public class ShutDownListener {

    @Inject
    protected ListenerSequenceRecorder recorder;

    @Inject
    protected Provider<RequestDetails> requestDetails;

    protected void event(@Observes ApplicationShutdownEvent event) {
        recorder.addToSequence(requestDetails.get().getCaller());
    }
}
