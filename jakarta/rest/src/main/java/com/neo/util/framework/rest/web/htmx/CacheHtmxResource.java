package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.rest.web.htmx.navigation.HtmxNavigationElement;
import com.neo.util.framework.rest.web.rest.CacheResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

@ApplicationScoped
@Path(CacheHtmxResource.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
@HtmxNavigationElement(name = "Cache", route = CacheHtmxResource.RESOURCE_LOCATION)
public class CacheHtmxResource {

    public static final String RESOURCE_LOCATION = "/admin/html/cache";

    public static final String P_RELOAD = "/reload";
    public static final String P_CLEAR = "/clear";

    protected CacheResource cacheResource;

    @Inject
    public CacheHtmxResource(CacheResource cacheResource) {
        this.cacheResource = cacheResource;
    }

    @GET
    public HtmlElement cacheName() {
        return HTML.
                """
                <h5 class="card-title">
                    Cache
                    <button type="button"
                        hx-post="\{RESOURCE_LOCATION + P_RELOAD}"
                        hx-swap="none"
                        class="btn btn-danger">
                            Reload Caches
                    </button>
                </h5>
                <table class="table table-striped">
                    <tbody>
                        \{cacheResource.getCacheNames().stream().map(this::getCacheRow)}
                    </tbody>
                </table>
                """;
    }

    protected HtmlElement getCacheRow(String cacheName) {
        return HTML.
                """
                <tr>
                    <td>\{cacheName}</td>
                    <td>
                        <button type="button"
                            hx-post="\{RESOURCE_LOCATION + "/" + cacheName + P_CLEAR}"}"
                            hx-swap="none"
                            class="btn btn-warning">
                                Clear Cache
                        </button>
                    </td>
                </tr>
                """ ;
    }

    @POST
    @Path(P_RELOAD)
    public void reload() {
        cacheResource.reload();
    }

    @POST
    @Path("/{id}" + P_CLEAR)
    public void clearCache(@PathParam("id") String id) {
        cacheResource.clearCache(id);
    }
}
