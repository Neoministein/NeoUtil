package com.neo.util.framework.rest.web.htmx;

import com.neo.util.framework.rest.web.rest.SchedulerResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.stream.Collectors;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTMX;

@ApplicationScoped
@Path(SchedulerHtmxResources.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
public class SchedulerHtmxResources {

    public static final String RESOURCE_LOCATION = "admin/html/scheduler";

    @Inject
    protected SchedulerResource schedulerResource;

    @GET
    public String getSchedulerConfig() {
        return schedulerResource.getSchedulerConfig().stream().map(config -> HTMX.
                """
                <div>
                    <div>\{config.getId()}</div>
                    <div>\{config.isEnabled()}</div>
                    <div>\{config.getContext()}</div>
                    <div>\{config.getLastExecution()}</div>
                    <div>\{config.getLastExecutionFailed()}</div>
                </div>
                """ ).collect(Collectors.joining());
    }
}
