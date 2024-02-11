package com.neo.util.framework.rest.web.htmx.navigation;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.impl.ReflectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

@ApplicationScoped
@Path(HtmxNavigation.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
public class HtmxNavigation {

    public static final String RESOURCE_LOCATION = "/admin/html/navigation";

    protected final HtmlElement navigationElement;

    @Inject
    public HtmxNavigation(ReflectionService reflectionService) {
        this.navigationElement = HTML."\{reflectionService.getAnnotationInstance(HtmxNavigationElement.class).stream().map(this::navigationElement)}";
    }

    @GET
    public HtmlElement getNavigation() {
        return navigationElement;
    }

    protected HtmlElement navigationElement(HtmxNavigationElement element) {
        return HTML."""
                <li class="nav-item">
                    <a class="nav-link" aria-current="page" hx-get="\{element.route()}" hx-target="#main">
                        \{element.name()}
                    </a>
                </li>
                """;
    }
}
