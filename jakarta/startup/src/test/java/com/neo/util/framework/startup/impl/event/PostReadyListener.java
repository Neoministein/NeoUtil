package com.neo.util.framework.startup.impl.event;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.startup.impl.ListenerSequenceRecorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@ApplicationScoped
public class PostReadyListener {

    @Inject
    protected ListenerSequenceRecorder recorder;

    @Inject
    protected Provider<RequestDetails> requestDetails;

    protected void event(@Observes ApplicationPostReadyEvent event) {
        recorder.addToSequence(requestDetails.get().getCaller());
    }

}
