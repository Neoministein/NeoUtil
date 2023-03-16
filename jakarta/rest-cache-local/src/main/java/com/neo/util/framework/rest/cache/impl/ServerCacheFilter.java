package com.neo.util.framework.rest.cache.impl;

import com.neo.util.framework.rest.cache.api.CachedObject;
import com.neo.util.framework.rest.api.cache.ServerCacheControl;
import com.neo.util.framework.rest.cache.api.ServerCacheService;
import com.neo.util.framework.rest.impl.RestUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Provider
@ApplicationScoped
@ServerCacheControl
public class ServerCacheFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCacheFilter.class);

    public static final String HEADER_NO_CACHE = "no-cache";
    public static final String HEADER_AGE = "age";

    @Inject
    protected ServerCacheService serverCacheService;

    @Inject
    protected RestUtils restUtils;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void filter(ContainerRequestContext request) {
        String noCache = request.getHeaderString(HEADER_NO_CACHE);
        if (noCache != null) {
            LOGGER.info("Request has header [no-cache] cache will be ignored");
            return;
        }

        ServerCacheControl serverCacheControl = restUtils.getAnnotation(ServerCacheControl.class).orElseThrow();

        String cacheKey = generateCacheKey(request.getMethod(), request.getUriInfo());
        LOGGER.debug("Checking server cache for key [{}]", cacheKey);

        final Optional<CachedObject> optionalCachedObject;
        if (serverCacheControl.expireAfter() == ServerCacheControl.EXPIRE_AFTER_DISABLED) {
            optionalCachedObject = serverCacheService.get(cacheKey);

        } else {
            optionalCachedObject = serverCacheService.getMaxAge(cacheKey, Duration.ofSeconds(serverCacheControl.expireAfter()));
        }

        if (optionalCachedObject.isEmpty()) {
            return;
        }

        CachedObject cachedObject = optionalCachedObject.get();
        LOGGER.debug("Cache object found for [{}] with age [{}]", cacheKey, cachedObject.getTimeSinceCached());

        request.abortWith(Response.ok(cachedObject.entity()).header(HEADER_AGE, cachedObject.getTimeSinceCached()).build());
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (response.getStatus() != 200 || response.getHeaderString(HEADER_AGE) != null) {
            return;
        }
        serverCacheService.put(generateCacheKey(request.getMethod(), request.getUriInfo()), new CachedObject(response.getEntity()));
    }


    protected String generateCacheKey(String method, UriInfo uriInfo) {
        StringBuilder sb = new StringBuilder(method).append(uriInfo.getPath()).append('?');
        for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');
        }

        return sb.substring(0, sb.length()-1);
    }
}
