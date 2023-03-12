package com.neo.util.framework.rest.impl.cache;

import com.neo.util.framework.rest.api.cache.ClientCacheControl;
import com.neo.util.framework.rest.api.cache.ServerCacheControl;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ServerCacheControl
@Path(CachedResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class CachedResource {

    public static final String RESOURCE_LOCATION = "/test/cacheControl";

    @GET
    @ClientCacheControl
    public String getTest() {
        return "Test";
    }

}
