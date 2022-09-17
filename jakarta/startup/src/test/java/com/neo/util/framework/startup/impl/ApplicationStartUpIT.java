package com.neo.util.framework.startup.impl;

import com.neo.util.framework.startup.impl.event.PostReadyListener;
import com.neo.util.framework.startup.impl.event.PreReadyListener;
import com.neo.util.framework.startup.impl.event.ReadyListener;
import com.neo.util.framework.startup.impl.event.ShutDownListener;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(WeldJunit5Extension.class)
class ApplicationStartUpIT {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            ApplicationStartUp.class,
            ListenerSequenceRecorder.class,
            ShutDownListener.class,
            PostReadyListener.class,
            ReadyListener.class,
            PreReadyListener.class
    ).activate(RequestScoped.class).build();

    protected ListenerSequenceRecorder recorder;


    @BeforeEach
    void init() {
        recorder = weld.select(ListenerSequenceRecorder.class).get();
    }

    @Test
    void applicationEventTest() {
        //Getting the list since it can't be access after the WeldContainer has been shutdown
        List<?> callSequence = recorder.getCallSequence();

        //Shutting down the Weld Container to trigger the {@link ApplicationShutdownEvent} otherwise assertion will be done before weld stops
        WeldContainer.current().shutdown();

        //List gets updated since it's the same instance as in the recorder
        Assertions.assertEquals(List.of(
                PreReadyListener.class,
                ReadyListener.class,
                PostReadyListener.class,
                ShutDownListener.class), callSequence);
    }
}
