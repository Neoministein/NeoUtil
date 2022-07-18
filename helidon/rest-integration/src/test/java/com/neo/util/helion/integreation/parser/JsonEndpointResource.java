package com.neo.util.helion.integreation.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.parser.ValidateJsonSchema;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(JsonEndpointResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class JsonEndpointResource {

    public static final String RESOURCE_LOCATION = "/test/json";

    public static final String P_PARSING = "/parsing";

    public static final String P_SCHEMA = "/schema";

    @POST
    @Path(P_PARSING)
    public Response jsonParsing(JsonNode jsonNode) {
        return Response.ok().entity(JsonUtil.toJson(jsonNode)).build();
    }

    @POST
    @Path(P_SCHEMA)
    @ValidateJsonSchema("schema/ITSchema.json")
    public Response jsonScheme(JsonNode jsonNode) {
        return Response.ok().entity(JsonUtil.toJson(jsonNode)).build();
    }
}
