package com.neo.util.framework.rest.web.rest;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

@ApplicationScoped
@Path(CacheResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class CacheResource {

    public static final String RESOURCE_LOCATION = "admin/api/cache";

    public static final ExceptionDetails EX_CACHE_DOES_NOT_EXIST = new ExceptionDetails("cache/invalid-id",
            "The provided cache id [{0}] does not exist.", false);

    @Inject
    protected CacheManager cacheManager;

    @GET
    public Set<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    @POST
    @Path("/reload")
    public void reload() {
        cacheManager.reload();
    }

    @POST
    @Path("/{id}/clear")
    public void clearCache(@QueryParam("id") String id) {
        Cache cache = cacheManager.getCache(id).orElseThrow(() -> new NoContentFoundException(EX_CACHE_DOES_NOT_EXIST, id));
        cache.invalidateAll();
    }
}
