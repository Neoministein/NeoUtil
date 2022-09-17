package com.neo.util.framework.startup.impl.event;

import com.neo.util.framework.api.event.ApplicationShutdownEvent;
import com.neo.util.framework.startup.impl.ListenerSequenceRecorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class ShutDownListener {

    @Inject
    protected ListenerSequenceRecorder recorder;

    protected void event(@Observes ApplicationShutdownEvent event) {
        recorder.addToSequence(this.getClass());
    }
}
