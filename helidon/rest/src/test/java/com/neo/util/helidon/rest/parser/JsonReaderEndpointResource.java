package com.neo.util.helidon.rest.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.parser.ValidateJsonSchema;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(JsonReaderEndpointResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class JsonReaderEndpointResource {

    public static final String RESOURCE_LOCATION = "/test/";

    public static final String P_IN_PARSING = "inbound/json/parsing";

    public static final String P_IN_SCHEMA = "inbound/json/schema";

    @POST
    @Path(P_IN_PARSING)
    public Response jsonParsing(JsonNode jsonNode) {
        return Response.ok().entity(JsonUtil.toJson(jsonNode)).build();
    }

    @POST
    @Path(P_IN_SCHEMA)
    @ValidateJsonSchema("ITSchema.json")
    public Response jsonScheme(JsonNode jsonNode) {
        return Response.ok().entity(JsonUtil.toJson(jsonNode)).build();
    }
}
