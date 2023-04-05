package com.neo.util.framework.rest.impl.cache;

import com.neo.util.framework.rest.api.cache.ClientCacheControl;
import com.neo.util.framework.rest.impl.RestResourceUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.RuntimeDelegate;

@ClientCacheControl
@Provider
public class ClientCacheControlFilter implements ContainerResponseFilter {

    @Inject
    protected RestResourceUtils restUtils;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == 200) {
            ClientCacheControl cacheAnnotation = restUtils.getAnnotation(ClientCacheControl.class).orElseThrow();

            CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(cacheAnnotation.maxAge());
            cacheControl.setSMaxAge(cacheAnnotation.sMaxAge());
            cacheControl.setPrivate(cacheAnnotation.privateFlag());
            cacheControl.setNoCache(cacheAnnotation.noCache());
            cacheControl.setNoStore(cacheAnnotation.noStore());
            cacheControl.setNoTransform(cacheAnnotation.noTransform());
            cacheControl.setProxyRevalidate(cacheAnnotation.proxyRevalidate());

            responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL,
                    RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class)
                            .toString(cacheControl));
            responseContext.getHeaders().add(HttpHeaders.EXPIRES, cacheAnnotation.maxAge());
        }
    }
}
