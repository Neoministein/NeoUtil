package com.neo.util.framework.rest.web.rest;

import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.excpetion.ToExternalException;
import com.neo.util.framework.impl.json.JsonSchemaLoader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path(JsonSchemaResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@ToExternalException({JsonSchemaLoader.E_SCHEMA_DOES_NOT_EXIST})
public class JsonSchemaResource {

    public static final String RESOURCE_LOCATION = "admin/api/json-schema";

    protected final JsonSchemaLoader jsonSchemaLoader;

    @Inject
    public JsonSchemaResource(JsonSchemaLoader jsonSchemaLoader) {
        this.jsonSchemaLoader = jsonSchemaLoader;
    }

    @GET
    public List<String> getSchemaNames() {
        return jsonSchemaLoader.getUnmodifiableMap().keySet().stream().sorted().toList();
    }

    @GET
    @Path("{path : .+}")//This param'{path : .+}' means that it also includes '/'
    public String getSchema(@PathParam("path") String path) {
        String rawJson = jsonSchemaLoader.requestJsonSchema(path)
                //Substring +6 is done since default toString start with '"#" :' which isn't valid json
                .toString().substring(6);


        return JsonUtil.toPrettyJson(JsonUtil.fromJson(rawJson));
    }
}
