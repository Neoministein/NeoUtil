package com.neo.util.framework.websocket;

import com.neo.util.framework.api.excpetion.ToExternalException;
import com.neo.util.framework.impl.json.JsonSchemaLoader;
import com.neo.util.framework.websocket.impl.MonitorableWebsocketScheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path(SocketTestResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@ToExternalException({JsonSchemaLoader.E_SCHEMA_DOES_NOT_EXIST})
public class SocketTestResource {

    public static final String RESOURCE_LOCATION = "test";

    protected final MonitorableWebsocketScheduler jsonSchemaLoader;

    @Inject
    public SocketTestResource(MonitorableWebsocketScheduler jsonSchemaLoader) {
        this.jsonSchemaLoader = jsonSchemaLoader;
    }

    @GET
    public String getSchemaNames() {
        jsonSchemaLoader.action();
        return "jsonSchemaLoader.getUnmodifiableMap().keySet().stream().sorted().toList();";
    }
}
