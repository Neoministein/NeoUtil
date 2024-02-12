package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.spi.CacheName;
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

    @Inject
    @CacheName("Test")
    protected Cache cache;

    @Inject
    protected CacheResource cacheResource;

    @GET
    public HtmlElement cacheName() {
        return HTML.
                """
                <h5 class="card-title">
                    Cache
                    <button type="button"
                        hx-post="\{RESOURCE_LOCATION}/reload"
                        hx-swap="none"
                        class="btn btn-danger">
                            Reload Caches
                    </button>
                </h5>
                <table class="table table-striped">
                    <tbody>
                        \{cacheResource.getCacheNames().stream().map(this::test)}
                    </tbody>
                </table>
                """;
    }

    public HtmlElement test(String cacheName) {
        return HTML.
                """
                <tr>
                    <td>\{cacheName}</td>
                    <td>
                        <button type="button"
                            hx-post="\{RESOURCE_LOCATION}/\{cacheName}/clear"}"
                            hx-swap="none"
                            class="btn btn-warning">
                                Clear Cache
                        </button>
                    </td>
                </tr>
                """ ;
    }

    @POST
    @Path("/reload")
    public void reload() {
        cacheResource.reload();
    }

    @POST
    @Path("/{id}/clear")
    public void clearCache(@PathParam("id") String id) {
        cacheResource.clearCache(id);
    }
}
