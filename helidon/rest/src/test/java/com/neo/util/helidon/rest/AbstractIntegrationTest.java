package com.neo.util.helidon.rest;

import com.neo.util.common.impl.json.JsonUtil;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import tools.jackson.databind.JsonNode;

public abstract class AbstractIntegrationTest {

    protected JsonNode validateResponse(Response response, int code) {
        Assertions.assertEquals(code, response.getStatus());
        return JsonUtil.fromJson(response.readEntity(String.class));
    }
}
