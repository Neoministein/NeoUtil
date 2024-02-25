package com.neo.util.framework.rest.impl.cache;

import com.neo.util.framework.rest.api.cache.ClientCacheControl;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import com.neo.util.framework.rest.impl.JaxResourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.concurrent.TimeUnit;

@ClientCacheControl
@Provider
@ApplicationScoped
public class ClientCacheControlFilter implements ContainerResponseFilter {

    @Inject
    protected JaxResourceUtils jaxResourceUtils;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        if (responseContext.getStatus() < 500 && (responseContext.getStatus() == 200 || responseContext.getHeaders().containsKey(
                ClientResponseService.VALID_BACKEND_ERROR))) {
            ClientCacheControl cacheAnnotation = jaxResourceUtils.getAnnotation(ClientCacheControl.class).orElseThrow();

            CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(parseTimeUnit(cacheAnnotation.maxAge(), cacheAnnotation.timeUnit()));
            cacheControl.setSMaxAge(parseTimeUnit(cacheAnnotation.sMaxAge(), cacheAnnotation.timeUnit()));
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

    protected int parseTimeUnit(int value, TimeUnit timeUnit) {
        if (value == -1) {
            return -1;
        }
        return (int) timeUnit.toSeconds(value);
    }
}
