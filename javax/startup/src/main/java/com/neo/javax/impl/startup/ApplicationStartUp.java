package com.neo.javax.impl.startup;

import com.neo.common.impl.StopWatch;
import com.neo.javax.api.event.ApplicationPostReadyEvent;
import com.neo.javax.api.event.ApplicationPreReadyEvent;
import com.neo.javax.api.event.ApplicationReadyEvent;
import com.neo.javax.api.event.ApplicationShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * This class start services
 */
@ApplicationScoped
public class ApplicationStartUp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartUp.class);

    @Inject
    Event<ApplicationPreReadyEvent> applicationPreReadyEventEvent;

    @Inject
    Event<ApplicationReadyEvent> applicationReadyEvent;

    @Inject
    Event<ApplicationPostReadyEvent> applicationPostReadyEvent;

    @Inject
    Event<ApplicationShutdownEvent> applicationShutdownEventEvent;

    /**
     * Fire initialization event
     */
    public void init( @Observes @Initialized( ApplicationScoped.class ) Object init ) {
        fireApplicationPreReadyEvent();
        fireApplicationReadyEvent();
        fireApplicationPostReadyEvent();
    }

    /**
     * Fire shutdown
     */
    public void destroy( @Observes @Destroyed( ApplicationScoped.class ) Object init ) {
        fireApplicationShutDownEvent();
    }


    protected void fireApplicationPreReadyEvent() {
        LOGGER.info("Fire Application pre ready event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationPreReadyEventEvent.fire(new ApplicationPreReadyEvent());
        stopWatch.stop();
        LOGGER.info("Application ready pre event took: {} ms ", stopWatch.getElapsedTimeMs());
    }

    protected void fireApplicationReadyEvent() {
        LOGGER.info("Fire Application ready event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationReadyEvent.fire(new ApplicationReadyEvent());
        stopWatch.stop();
        LOGGER.info("Application Init event took: {} ms ", stopWatch.getElapsedTimeMs());
    }

    protected void fireApplicationPostReadyEvent() {
        LOGGER.info("Fire Application post ready event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationPostReadyEvent.fire(new ApplicationPostReadyEvent());
        stopWatch.stop();
        LOGGER.info("Application ready post event took: {} ms ", stopWatch.getElapsedTimeMs());
    }

    protected void fireApplicationShutDownEvent() {
        LOGGER.info("Fire Application shutdown event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationShutdownEventEvent.fire(new ApplicationShutdownEvent());
        stopWatch.stop();
        LOGGER.info("Application shutdown took: {} ms ", stopWatch.getElapsedTimeMs());
    }
}
