package com.neo.util.framework.startup.impl.event;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.startup.impl.ListenerSequenceRecorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@ApplicationScoped
public class ReadyListener {

    @Inject
    protected ListenerSequenceRecorder recorder;

    @Inject
    protected Provider<RequestDetails> requestDetails;

    protected void event(@Observes ApplicationReadyEvent event) {
        recorder.addToSequence(requestDetails.get().getInitiator());
    }
}
