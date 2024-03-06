package com.neo.util.framework.rest.web.rest;

import com.neo.util.framework.api.cache.CacheManager;
import com.neo.util.framework.api.excpetion.ToExternalException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

@ApplicationScoped
@Path(CacheResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@ToExternalException({CacheManager.E_CACHE_DOES_NOT_EXIST})
public class CacheResource {

    public static final String RESOURCE_LOCATION = "admin/api/cache";

    protected final CacheManager cacheManager;

    @Inject
    public CacheResource(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GET
    public Set<String> getCacheNames() {
        return cacheManager.fetchCacheNames();
    }

    @POST
    @Path("/reload")
    public void reload() {
        cacheManager.reload();
    }

    @POST
    @Path("/{id}/clear")
    public void clearCache(@PathParam("id") String id) {
        cacheManager.requestCache(id).invalidateAll();
    }
}
