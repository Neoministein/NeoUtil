package com.neo.util.helidon.rest.exception;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.InternalRuntimeException;
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
    public static final String P_INTERNAL_LOGIC = "/internalException";
    public static final String P_EXTERNAL_JSON = "/externalException";
    public static final String P_CLIENT_ERROR = "/clientError";

    public static final ExceptionDetails EX_INTERNAL_COMMON_RUNTIME = new ExceptionDetails(
            "test/internal-common-runtime", "CommonRuntimeException");

    public static final ExceptionDetails EX_EXTERNAL_COMMON_RUNTIME = new ExceptionDetails(
            "test/external-common-runtime", "CommonRuntimeException");

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
    public Response internalException() {
        throw new InternalRuntimeException(EX_INTERNAL_COMMON_RUNTIME);
    }

    @GET
    @Path(P_EXTERNAL_JSON)
    public Response externalException() {
        throw new InternalRuntimeException(EX_EXTERNAL_COMMON_RUNTIME);
    }

    @GET
    @Path(P_CLIENT_ERROR)
    public Response clientError() {
        throw new ClientErrorException("Test Client Exception" ,404);
    }
}
