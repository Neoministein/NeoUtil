package com.neo.util.helidon.rest.security;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.rest.api.security.Secured;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SecurityResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class SecurityResource {

    public static final String RESOURCE_LOCATION = "/test/security";

    public static final String P_SECURED = "/secure";

    public static final String P_ROLE = "/role";

    @Inject
    protected RequestDetails requestDetails;

    @GET
    @Secured
    @Path(P_SECURED)
    public Response jsonParsing() {
        return Response.ok().entity(requestDetails.getUser().get().getName()).build();
    }

    @GET
    @Secured
    @RolesAllowed("ADMIN")
    @Path(P_ROLE)
    public Response jsonScheme() {
        return Response.ok().entity(requestDetails.getUser().get().getName()).build();
    }
}
