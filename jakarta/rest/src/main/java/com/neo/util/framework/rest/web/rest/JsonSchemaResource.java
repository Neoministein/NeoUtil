package com.neo.util.framework.rest.web.rest;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.common.impl.json.JsonUtil;
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
public class JsonSchemaResource {

    public static final String RESOURCE_LOCATION = "admin/api/json-schema";

    public static final ExceptionDetails EX_SCHEMA_DOES_NOT_EXIST = new ExceptionDetails("common/json/schema/i<<<nvalid-path",
            "The provided json schema path [{0}] does not exist.", false);

    @Inject
    protected JsonSchemaLoader jsonSchemaLoader;

    @GET
    public List<String> getSchemaNames() {
        return jsonSchemaLoader.getUnmodifiableMap().keySet().stream().sorted().toList();
    }

    @GET
    @Path("{path : .+}")//This param'{path : .+}' means that it also includes '/'
    public String getSchema(@PathParam("path") String path) {
        String rawJson = jsonSchemaLoader.getJsonSchema(path)
                .orElseThrow(() -> new NoContentFoundException(EX_SCHEMA_DOES_NOT_EXIST, path))
                //Substring +6 is done since default toString start with '"#" :' which isn't valid json
                .toString().substring(6);


        return JsonUtil.toPrettyJson(JsonUtil.fromJson(rawJson));
    }
}
