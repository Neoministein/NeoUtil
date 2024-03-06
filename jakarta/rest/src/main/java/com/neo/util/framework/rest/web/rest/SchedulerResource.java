package com.neo.util.framework.rest.web.rest;

import com.neo.util.framework.api.excpetion.ToExternalException;
import com.neo.util.framework.api.scheduler.SchedulerConfig;
import com.neo.util.framework.api.scheduler.SchedulerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path(SchedulerResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@ToExternalException({SchedulerService.E_INVALID_SCHEDULER_ID})
public class SchedulerResource {

    public static final String RESOURCE_LOCATION = "/admin/api/scheduler";

    protected final SchedulerService schedulerService;

    @Inject
    public SchedulerResource(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GET
    @Path("{id}")
    public SchedulerConfig getSchedulerConfig(@PathParam("id") String id) {
        return schedulerService.requestSchedulerConfig(id);
    }

    @GET
    public List<SchedulerConfig> getSchedulerConfig() {
        return schedulerService.getSchedulerIds().stream()
                .map(schedulerService::requestSchedulerConfig)
                .toList();
    }

    @GET
    @Path("/active")
    public List<SchedulerConfig> getActiveSchedulerConfig() {
        return schedulerService.getSchedulerIds().stream()
                .map(schedulerService::requestSchedulerConfig)
                .filter(SchedulerConfig::isEnabled)
                .toList();
    }

    @POST
    @Path("/execute/{id}")
    public void executeScheduler(@PathParam("id") String id) {
        schedulerService.execute(id);
    }

    @POST
    @Path("/start/{id}")
    public void startScheduler(@PathParam("id") String id) {
        schedulerService.start(id);
    }

    @POST
    @Path("/stop/{id}")
    public void stopScheduler(@PathParam("id") String id) {
        schedulerService.stop(id);
    }
}
