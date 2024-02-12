package com.neo.util.framework.rest.web.rest;

import com.neo.util.framework.api.janitor.JanitorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path(JanitorResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class JanitorResource {

    public static final String RESOURCE_LOCATION = "/admin/api/janitor";

    protected final JanitorService janitorService;

    @Inject
    public JanitorResource(JanitorService janitorService) {
        this.janitorService = janitorService;
    }
/*
    @GET
    public Set<String> getJanitorNames() {
        return janitorService.getJanitorNames();
    }

    @POST
    public void executeAll() {
        janitorService.executeAll();
    }

    @POST
    @Path("{id}")
    public void execute(@PathParam("id") String id) {
        janitorService.execute(id);
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
    }*/
}
