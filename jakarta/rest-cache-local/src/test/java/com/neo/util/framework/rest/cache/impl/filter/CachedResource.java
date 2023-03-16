package com.neo.util.framework.rest.cache.impl.filter;

import com.neo.util.framework.rest.api.cache.ServerCacheControl;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ServerCacheControl
@Path(CachedResource.RESOURCE_LOCATION)
public class CachedResource {

    public static final String RESOURCE_LOCATION = "/test/cacheControl";
    public static final String P_INCREMENTAL_VALUE = "/incr";
    public static final String P_EXPIRED = "/exp";

    protected int incrementalValue = 0;

    @GET
    @Path(P_INCREMENTAL_VALUE)
    public int getIncrementalValue() {
        return incrementalValue++;
    }


    @GET
    @Path(P_EXPIRED)
    @ServerCacheControl(expireAfter = 0)
    public int getTest() {
        return incrementalValue++;
    }

}
