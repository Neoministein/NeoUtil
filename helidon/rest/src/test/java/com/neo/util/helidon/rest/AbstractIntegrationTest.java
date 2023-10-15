package com.neo.util.helidon.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.helidon.rest.connection.DefaultPersistenceContext;
import io.helidon.microprofile.testing.junit5.AddBean;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;

@AddBean( value = DefaultPersistenceContext.class)
public abstract class AbstractIntegrationTest {

    protected JsonNode validateResponse(Response response, int code) {
        Assertions.assertEquals(code, response.getStatus());
        return JsonUtil.fromJson(response.readEntity(String.class));
    }
}
