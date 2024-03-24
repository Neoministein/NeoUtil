package com.neo.util.helidon.rest.security;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.security.SecuredResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(SecurityResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RequestScoped
public class SecurityResource {

    public static final String RESOURCE_LOCATION = "/test/security";

    public static final String P_SECURED = "/secure";

    public static final String P_ROLE = "/role";

    @Inject
    protected RequestDetails requestDetails;

    @GET
    public Response unsecured() {
        return Response.ok().build();
    }

    @GET
    @SecuredResource
    @Path(P_SECURED)
    public Response jsonParsing() {
        return Response.ok().entity(((HttpRequestDetails) requestDetails).getUser().get().getName()).build();
    }

    @GET
    @SecuredResource
    @RolesAllowed("ADMIN")
    @Path(P_ROLE)
    public Response jsonScheme() {
        return Response.ok().entity(((HttpRequestDetails) requestDetails).getUser().get().getName()).build();
    }
}
