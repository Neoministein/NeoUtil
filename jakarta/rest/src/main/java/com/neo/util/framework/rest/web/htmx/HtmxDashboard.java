package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.impl.ReflectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

@ApplicationScoped
@Path(HtmxDashboard.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
public class HtmxDashboard {

    public static final String RESOURCE_LOCATION = "/admin/";

    protected final HtmlElement navigationElement;
    protected final String dashboard;

    @Inject
    public HtmxDashboard(ReflectionService reflectionService, ConfigService configService) {
        this.navigationElement = HTML."\{reflectionService.getAnnotationInstance(HtmxNavigationElement.class).stream().map(this::navigationElement)}";
        this.dashboard = ResourceUtil.getResourceFileAsString(configService.get("admin.dashboard").asString().orElse("static/dashboard.html"));
    }

    @GET
    @Path("/dashboard")
    public String getDashboard() {
        return dashboard;
    }

    @GET
    @Path("/html/navigation")
    public HtmlElement getNavigation() {
        return navigationElement;
    }

    protected HtmlElement navigationElement(HtmxNavigationElement element) {
        return HTML."""
                <li class="nav-item">
                    <a class="nav-link" aria-current="page" hx-get="\{element.route()}" hx-target="#main" href="/\{element.name()}">
                        \{element.name()}
                    </a>
                </li>
                """;
    }
}
