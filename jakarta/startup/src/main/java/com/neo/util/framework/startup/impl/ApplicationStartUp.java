package com.neo.util.framework.startup.impl;

import com.neo.util.common.impl.StopWatch;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.event.ApplicationShutdownEvent;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Inject
    protected InstanceIdentification identification;

    /**
     * Fire initialization event
     */
    public void init( @Observes @Priority( PriorityConstants.LIBRARY_BEFORE ) @Initialized( ApplicationScoped.class ) Object init ) {
        try {
            requestContextExecutor.execute(new StartupRequestDetails(
                    identification.getInstanceId(), ApplicationPreReadyEvent.EVENT_NAME), this::fireApplicationPreReadyEvent);
            requestContextExecutor.execute(new StartupRequestDetails(
                    identification.getInstanceId(), ApplicationReadyEvent.EVENT_NAME), this::fireApplicationReadyEvent);
            requestContextExecutor.execute(new StartupRequestDetails(
                    identification.getInstanceId(), ApplicationPostReadyEvent.EVENT_NAME), this::fireApplicationPostReadyEvent);
        } catch (Exception ex) {
            LOGGER.error("An error occurred during the startup process. Throwing DeploymentException", ex);
            throw new DeploymentException(ex);
        }
    }

    /**
     * Fire shutdown
     */
    public void destroy( @Observes @Priority( PriorityConstants.LIBRARY_BEFORE ) @Destroyed( ApplicationScoped.class ) Object init ) {
        requestContextExecutor.execute(new StartupRequestDetails(
                identification.getInstanceId(), ApplicationShutdownEvent.EVENT_NAME), this::fireApplicationShutDownEvent);
    }


    protected void fireApplicationPreReadyEvent() {
        LOGGER.info("Fire Application pre ready event");
        StopWatch stopWatch = new StopWatch().start();
        applicationPreReadyEventEvent.fire(new ApplicationPreReadyEvent());
        LOGGER.info("Application ready pre event took: [{}] ms ", stopWatch.stop().getElapsedTimeMs());
    }

    protected void fireApplicationReadyEvent() {
        LOGGER.info("Fire Application ready event");
        StopWatch stopWatch = new StopWatch().start();
        applicationReadyEvent.fire(new ApplicationReadyEvent());
        LOGGER.info("Application Init event took: [{}] ms ", stopWatch.stop().getElapsedTimeMs());
    }

    protected void fireApplicationPostReadyEvent() {
        LOGGER.info("Fire Application post ready event");
        StopWatch stopWatch = new StopWatch().start();
        applicationPostReadyEvent.fire(new ApplicationPostReadyEvent());
        LOGGER.info("Application ready post event took: [{}] ms ", stopWatch.stop().getElapsedTimeMs());
    }

    protected void fireApplicationShutDownEvent() {
        LOGGER.info("Fire Application shutdown event");
        StopWatch stopWatch = new StopWatch().start();
        applicationShutdownEventEvent.fire(new ApplicationShutdownEvent());
        LOGGER.info("Application shutdown took: [{}] ms ", stopWatch.stop().getElapsedTimeMs());
    }
}
