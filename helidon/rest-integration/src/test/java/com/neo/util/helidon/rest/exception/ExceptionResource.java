package com.neo.util.helidon.rest.exception;

import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.InternalLogicException;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(ExceptionResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class ExceptionResource {

    public static final String RESOURCE_LOCATION = "/test/exception";

    public static final String P_SUCCESS = "/success";
    public static final String P_RUNTIME = "/runtime";
    public static final String P_INTERNAL_LOGIC = "/internalLogic";
    public static final String P_INTERNAL_JSON = "/internalJson";
    public static final String P_CLIENT_ERROR = "/clientError";

    @GET
    @Path(P_SUCCESS)
    public Response success() {
        return Response.ok().build();
    }

    @GET
    @Path(P_RUNTIME)
    public Response runtime() {
        throw new RuntimeException("Test Runtime Exception");
    }

    @GET
    @Path(P_INTERNAL_LOGIC)
    public Response internalLogic() {
        throw new InternalLogicException("Test Internal Logic Exception");
    }

    @GET
    @Path(P_INTERNAL_JSON)
    public Response internalJson() {
        throw new InternalJsonException("Test Internal Json Exception");
    }

    @GET
    @Path(P_CLIENT_ERROR)
    public Response clientError() {
        throw new ClientErrorException("Test Client Exception" ,404);
    }
}