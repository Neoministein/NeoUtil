package com.neo.util.helidon.rest.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.parser.InboundDto;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(InboundParserResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class InboundParserResource {

    public static final String RESOURCE_LOCATION = "/test/parsing";
    public static final String P_BASIC = "/basic";
    public static final String P_ADVANCED = "/advanced";

    @POST
    @Path(P_BASIC)
    public Response dtoParsing(BasicInboundDto basicInboundDto) {
        return Response.ok().entity(JsonUtil.toJson(basicInboundDto)).build();
    }

    @POST
    @Path(P_ADVANCED)
    public Response dtoParsing(AdvancedInboundDto advancedInboundDto) {
        return Response.ok().entity(JsonUtil.toJson(advancedInboundDto)).build();
    }

    @InboundDto
    public record BasicInboundDto(
            @JsonProperty(required = true)
            String aString,
            @JsonProperty(required = true)
            int aNumber
    ) {}

    @InboundDto
    public record AdvancedInboundDto(
            @JsonProperty(value = "a_different_name", required = true)
            String aString,
            @JsonProperty(required = true)
            boolean aBoolean
    ) {}
}
