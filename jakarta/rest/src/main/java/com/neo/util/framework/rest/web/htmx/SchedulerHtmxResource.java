package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.api.scheduler.SchedulerConfig;
import com.neo.util.framework.rest.web.htmx.navigation.HtmxNavigationElement;
import com.neo.util.framework.rest.web.rest.SchedulerResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

@ApplicationScoped
@Path(SchedulerHtmxResource.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
@HtmxNavigationElement(name = "Scheduler", route = SchedulerHtmxResource.RESOURCE_LOCATION)
public class SchedulerHtmxResource {

    public static final String RESOURCE_LOCATION = "/admin/html/scheduler";

    public static final String P_START = "/start/";
    public static final String P_STOP = "/stop/";

    public static final String P_EXECUTE = "/execute/";

    protected final SchedulerResource schedulerResource;
    protected final ResourceFormattingService resourceFormttingService;

    @Inject
    public SchedulerHtmxResource(SchedulerResource schedulerResource, ResourceFormattingService resourceFormttingService) {
        this.schedulerResource = schedulerResource;
        this.resourceFormttingService = resourceFormttingService;
    }


    @GET
    public HtmlElement getSchedulerConfig() {
        return HTML.
                """
                <h5 class="card-title">Scheduler</h5>
                <table class="table table-striped">
                    <tbody>
                        \{schedulerResource.getSchedulerConfig().stream().map(this::parseFullConfig)}
                    </tbody>
                </table>
                """;
    }

    @POST
    @Path(P_START + "{id}")
    public HtmlElement startScheduler(@PathParam("id") String id) {
        schedulerResource.startScheduler(id);
        return parseToggleButton(schedulerResource.getSchedulerConfig(id));
    }

    @POST
    @Path(P_STOP + "{id}")
    public HtmlElement stopScheduler(@PathParam("id") String id) {
        schedulerResource.stopScheduler(id);
        return parseToggleButton(schedulerResource.getSchedulerConfig(id));
    }

    @POST
    @Path(P_EXECUTE + "{id}")
    public HtmlElement executeScheduler(@PathParam("id") String id) {
        schedulerResource.executeScheduler(id);
        return parseFullConfig(schedulerResource.getSchedulerConfig(id));
    }

    public HtmlElement parseFullConfig(SchedulerConfig config) {
        return HTML.
                """
                <tr id="\{config.getId()}">
                    <td>\{config.getId()}</td>
                    <td>
                        <div class="form-check form-switch">
                            \{parseToggleButton(config)}
                            <button type="button"
                                hx-post="\{RESOURCE_LOCATION + P_EXECUTE + config.getId()}"
                                hx-target="#\{config.getId()}"
                                hx-swap="outerHTML"
                                class="btn btn-light">
                                    =>
                            </button>
                        </div>
                    </td>
                    <td class="col">Last Execution: \{resourceFormttingService.toDateSecond(config.getLastExecution())}</td>
                </tr>
                """ ;
    }

    public HtmlElement parseToggleButton(SchedulerConfig config) {
        return HTML."""
                <input type="checkbox"
                       role="switch"
                       hx-post="\{RESOURCE_LOCATION + (config.isEnabled() ? P_STOP : P_START)}\{config.getId()}"
                       hx-swap="outerHTML"
                       hx-trigger="click"
                       class="form-check-input" \{config.isEnabled() ? "checked" : ""}/>
                """;
    }
}
