package com.neo.util.framework.rest.impl.cache;

import com.neo.util.framework.rest.api.cache.CacheControl;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.RuntimeDelegate;

@CacheControl
@Provider
public class CacheControlResponseFilter implements ContainerResponseFilter {

    @Context
    protected ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == 200) {
            CacheControl cacheAnnotation = resourceInfo.getResourceMethod().getAnnotation(CacheControl.class);
            if (cacheAnnotation == null) {
                cacheAnnotation = resourceInfo.getResourceClass().getAnnotation(CacheControl.class);
            }

            jakarta.ws.rs.core.CacheControl cacheControl = new jakarta.ws.rs.core.CacheControl();
            cacheControl.setMaxAge(cacheAnnotation.maxAge());
            cacheControl.setSMaxAge(cacheAnnotation.sMaxAge());
            cacheControl.setPrivate(cacheAnnotation.privateFlag());
            cacheControl.setNoCache(cacheAnnotation.noCache());
            cacheControl.setNoStore(cacheAnnotation.noStore());
            cacheControl.setNoTransform(cacheAnnotation.noTransform());
            cacheControl.setProxyRevalidate(cacheAnnotation.proxyRevalidate());

            responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL,
                    RuntimeDelegate.getInstance().createHeaderDelegate(jakarta.ws.rs.core.CacheControl.class)
                            .toString(cacheControl));
            responseContext.getHeaders().add(HttpHeaders.EXPIRES, cacheAnnotation.maxAge());
        }
    }
}
