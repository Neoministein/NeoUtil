package com.neo.util.framework.rest.web.rest;

import com.neo.util.framework.api.janitor.JanitorConfig;
import com.neo.util.framework.api.janitor.JanitorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

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

    @GET
    @Path("{id}")
    public JanitorConfig getJanitorConfig(@PathParam("id") String janitorId) {
        return janitorService.getJanitorConfig(janitorId);
    }
    @GET
    public List<JanitorConfig> getJanitorConfig() {
        return janitorService.getJanitorIds().stream()
                .map(janitorService::getJanitorConfig)
                .toList();
    }

    @POST
    public void executeAll() {
        janitorService.executeAll();
    }

    @POST
    @Path("{id}")
    public void execute(@PathParam("id") String janitorId) {
        janitorService.execute(janitorId);
    }

    @POST
    @Path("/enable/{id}")
    public void enable(@PathParam("id") String janitorId) {
        janitorService.enable(janitorId);
    }

    @POST
    @Path("/disable/{id}")
    public void disable(@PathParam("id") String janitorId) {
        janitorService.disable(janitorId);
    }
}
