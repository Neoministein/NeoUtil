package com.neo.util.framework.rest.web.rest;

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
public class SchedulerResource {

    public static final String RESOURCE_LOCATION = "/admin/api/scheduler";

    @Inject
    protected SchedulerService schedulerService;

    @GET
    @Path("{id}")
    public SchedulerConfig getSchedulerConfig(@PathParam("id") String id) {
        return schedulerService.getSchedulerConfig(id);
    }

    @GET
    public List<SchedulerConfig> getSchedulerConfig() {
        return schedulerService.getSchedulerIds().stream()
                .map(ids -> schedulerService.getSchedulerConfig(ids))
                .toList();
    }

    @GET
    @Path("/active")
    public List<SchedulerConfig> getActiveSchedulerConfig() {
        return schedulerService.getSchedulerIds().stream()
                .map(ids -> schedulerService.getSchedulerConfig(ids))
                .filter(SchedulerConfig::isEnabled)
                .toList();
    }

    @POST
    @Path("/execute/{id}")
    public void executeScheduler(@PathParam("id") String id) {
        schedulerService.executeScheduler(id);
    }

    @POST
    @Path("/start/{id}")
    public void startScheduler(@PathParam("id") String id) {
        schedulerService.startScheduler(id);
    }

    @POST
    @Path("/stop/{id}")
    public void stopScheduler(@PathParam("id") String id) {
        schedulerService.stopScheduler(id);
    }
}
