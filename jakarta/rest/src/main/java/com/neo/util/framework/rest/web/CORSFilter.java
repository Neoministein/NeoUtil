package com.neo.util.framework.rest.web;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
@ApplicationScoped
public class CORSFilter implements ContainerResponseFilter {

    protected final List<String> cors;

    @Inject
    public CORSFilter(ConfigService configService) {
        this.cors = configService.getAsList(String.class, x -> x, "security.cors").orElse(List.of());
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        String origin = requestContext.getHeaderString("Origin");

        if (origin != null && cors.contains(origin)) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization, credentials");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "600");
        responseContext.getHeaders().putSingle("Access-Control-Expose-Headers",
                ClientResponseService.VALID_BACKEND_ERROR + ", Set-Cookie");
    }

}