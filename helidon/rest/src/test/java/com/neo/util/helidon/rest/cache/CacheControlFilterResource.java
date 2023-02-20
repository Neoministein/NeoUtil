package com.neo.util.helidon.rest.cache;

import com.neo.util.framework.rest.api.cache.CacheControl;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(CacheControlFilterResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class CacheControlFilterResource {

    public static final String RESOURCE_LOCATION = "/test/cacheControll";

    public static final String P_MAX_AGE = "/maxAge";

    public static final int MAX_AGE = 86400;

    public static final String ENTITY = "Hello World";

    @GET
    @Path(P_MAX_AGE)
    @CacheControl(maxAge = MAX_AGE)
    public String dtoParsing() {
        return ENTITY;
    }
}
