package com.neo.util.framework.startup.impl.event;

import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.startup.impl.ListenerSequenceRecorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class PreReadyListener {

    @Inject
    protected ListenerSequenceRecorder recorder;

    protected void event(@Observes ApplicationPreReadyEvent event) {
        recorder.addToSequence(this.getClass());
    }

}
