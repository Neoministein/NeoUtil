package com.neo.util.framework.startup.impl;

import com.neo.util.common.impl.StopWatch;;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.event.ApplicationShutdownEvent;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.annotation.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * This class start services
 */
@ApplicationScoped
public class ApplicationStartUp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartUp.class);

    @Inject
    protected Event<ApplicationPreReadyEvent> applicationPreReadyEventEvent;

    @Inject
    protected Event<ApplicationReadyEvent> applicationReadyEvent;

    @Inject
    protected Event<ApplicationPostReadyEvent> applicationPostReadyEvent;

    @Inject
    protected Event<ApplicationShutdownEvent> applicationShutdownEventEvent;

    @Inject
    protected RequestContextExecutor requestContextExecutor;

    /**
     * Fire initialization event
     */
    public void init( @Observes @Priority( PriorityConstants.LIBRARY_BEFORE ) @Initialized( ApplicationScoped.class ) Object init ) {
        requestContextExecutor.execute(new StartupRequestDetails(ApplicationPreReadyEvent.EVENT_NAME), this::fireApplicationPreReadyEvent);
        requestContextExecutor.execute(new StartupRequestDetails(ApplicationReadyEvent.EVENT_NAME), this::fireApplicationReadyEvent);
        requestContextExecutor.execute(new StartupRequestDetails(ApplicationPostReadyEvent.EVENT_NAME), this::fireApplicationPostReadyEvent);
    }

    /**
     * Fire shutdown
     */
    public void destroy( @Observes @Priority( PriorityConstants.LIBRARY_BEFORE ) @Destroyed( ApplicationScoped.class ) Object init ) {
        requestContextExecutor.execute(new StartupRequestDetails(ApplicationShutdownEvent.EVENT_NAME), this::fireApplicationShutDownEvent);
    }


    protected void fireApplicationPreReadyEvent() {
        LOGGER.info("Fire Application pre ready event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationPreReadyEventEvent.fire(new ApplicationPreReadyEvent());
        stopWatch.stop();
        LOGGER.info("Application ready pre event took: [{}] ms ", stopWatch.getElapsedTimeMs());
    }

    protected void fireApplicationReadyEvent() {
        LOGGER.info("Fire Application ready event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationReadyEvent.fire(new ApplicationReadyEvent());
        stopWatch.stop();
        LOGGER.info("Application Init event took: [{}] ms ", stopWatch.getElapsedTimeMs());
    }

    protected void fireApplicationPostReadyEvent() {
        LOGGER.info("Fire Application post ready event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationPostReadyEvent.fire(new ApplicationPostReadyEvent());
        stopWatch.stop();
        LOGGER.info("Application ready post event took: [{}] ms ", stopWatch.getElapsedTimeMs());
    }

    protected void fireApplicationShutDownEvent() {
        LOGGER.info("Fire Application shutdown event");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        applicationShutdownEventEvent.fire(new ApplicationShutdownEvent());
        stopWatch.stop();
        LOGGER.info("Application shutdown took: [{}] ms ", stopWatch.getElapsedTimeMs());
    }
}
