package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.api.janitor.JanitorConfig;
import com.neo.util.framework.rest.web.htmx.navigation.HtmxNavigationElement;
import com.neo.util.framework.rest.web.rest.JanitorResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

@ApplicationScoped
@Path(JanitorHtmxResource.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
@HtmxNavigationElement(name = "Janitor", route = JanitorHtmxResource.RESOURCE_LOCATION)
public class JanitorHtmxResource {

    public static final String RESOURCE_LOCATION = "/admin/html/janitor";

    public static final String P_ENABLE = "/enable/";
    public static final String P_DISABLE = "/disable/";

    public static final String P_EXECUTE = "/execute/";

    protected final JanitorResource janitorResource;
    protected final ResourceFormattingService resourceFormattingService;

    @Inject
    public JanitorHtmxResource(JanitorResource janitorResource, ResourceFormattingService resourceFormattingService) {
        this.janitorResource = janitorResource;
        this.resourceFormattingService = resourceFormattingService;
    }

    @GET
    public HtmlElement getJanitorConfig() {
        return HTML.
                """
                <h5 class="card-title">
                    Janitors
                    <button type="button"
                        hx-post="\{RESOURCE_LOCATION}"
                        hx-swap="none"
                        class="btn btn-danger">
                            Execute All
                    </button>
                </h5>
                <table class="table table-striped">
                    <tbody>
                        \{janitorResource.getJanitorConfig().stream().map(this::parseFullConfig)}
                    </tbody>
                </table>
                """;
    }

    @POST
    public void executeAll() {
        janitorResource.executeAll();
    }

    @POST
    @Path(P_EXECUTE + "{id}")
    public HtmlElement execute(@PathParam("id") String janitorId) {
        janitorResource.execute(janitorId);
        return parseFullConfig(janitorResource.getJanitorConfig(janitorId));
    }

    @POST
    @Path(P_ENABLE + "{id}")
    public HtmlElement enable(@PathParam("id") String janitorId) {
        janitorResource.enable(janitorId);
        return parseToggleButton(janitorResource.getJanitorConfig(janitorId));
    }

    @POST
    @Path(P_DISABLE + "{id}")
    public HtmlElement disable(@PathParam("id") String janitorId) {
        janitorResource.disable(janitorId);
        return parseToggleButton(janitorResource.getJanitorConfig(janitorId));
    }

    public HtmlElement parseFullConfig(JanitorConfig config) {
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
                    <td class="col">Last Execution: \{resourceFormattingService.toDateSecond(config.getLastExecution())}</td>
                </tr>
                """ ;
    }

    public HtmlElement parseToggleButton(JanitorConfig config) {
        return HTML."""
                <input type="checkbox"
                       role="switch"
                       hx-post="\{RESOURCE_LOCATION + (config.isEnabled() ? P_DISABLE : P_ENABLE)}\{config.getId()}"
                       hx-swap="outerHTML"
                       hx-trigger="click"
                       class="form-check-input" \{config.isEnabled() ? "checked" : ""}/>
                """;
    }


}
