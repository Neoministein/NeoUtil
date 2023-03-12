package com.neo.util.framework.rest.cache.impl;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.rest.api.cache.ServerCacheControl;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Provider
@ApplicationScoped
@ServerCacheControl
public class CaffeineServerCache implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    protected ResourceInfo resourceInfo;

    protected Cache<String, Object> cache;

    @PostConstruct
    public void init() {
        cache = Caffeine.newBuilder()
                .build();


    }

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        System.out.println();
        /*
        String cacheKey = generateCacheKey(request.getUriInfo());

        Object cacheObject = cache.getIfPresent(cacheKey);
        if (cacheObject == null) {
            return;
        }
        */


        //request.abortWith(Response.ok("cacheObject").header("","").build());
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (response.getStatus() != 200) {
            return;
        }

        //cache.put(request);
    }


    protected String generateCacheKey(UriInfo uriInfo) {
        StringBuilder sb = new StringBuilder(uriInfo.getPath()).append('?');
        for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');
        }

        return sb.substring(0, sb.length()-1);
    }
}
