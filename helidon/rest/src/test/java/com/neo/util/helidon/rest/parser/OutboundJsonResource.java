package com.neo.util.helidon.rest.parser;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.parser.OutboundJsonView;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(OutboundJsonResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class OutboundJsonResource {

    public static final String RESOURCE_LOCATION = "/test/outbound/json";

    public static final String P_BASIC = "/basic";

    public static final String P_VIEW_PUBLIC = "/view/public";

    public static final String P_VIEW_OWNER = "/view/owner";

    public static final String P_VIEW_INTERNAL = "/view/internal";

    public static final String P_OUT_PARSING = "/parsing";

    public static final BasicOutboundDto OUTBOUND_DTO = new BasicOutboundDto(
            "basic",
            "public",
            "owner",
            "internal"
    );

    public static final JsonNode OUTBOUND_JSON = JsonUtil.emptyObjectNode()
            .put("string","AString")
            .put("integer",1000)
            .put("boolean",true);

    @GET
    @Path(P_BASIC)
    public Response basic() {
        return Response.ok().entity(OUTBOUND_DTO).build();
    }

    @GET
    @Path(P_VIEW_PUBLIC)
    @OutboundJsonView(Views.Public.class)
    public Response viewPublic() {
        return Response.ok().entity(OUTBOUND_DTO).build();
    }

    @GET
    @Path(P_VIEW_OWNER)
    @OutboundJsonView(Views.Owner.class)
    public Response viewOwner() {
        return Response.ok().entity(OUTBOUND_DTO).build();
    }

    @GET
    @Path(P_VIEW_INTERNAL)
    @OutboundJsonView(Views.Internal.class)
    public Response viewInternal() {
        return Response.ok().entity(OUTBOUND_DTO).build();
    }


    @GET
    @Path(P_OUT_PARSING)
    public JsonNode getJsonNode() {
        return OUTBOUND_JSON;
    }

    public record BasicOutboundDto(
            String basicValue,
            @JsonView(Views.Public.class)
            String publicValue,
            @JsonView(Views.Owner.class)
            String ownerValue,
            @JsonView(Views.Internal.class)
            String internalValue
    ) {}
}
