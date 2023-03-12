package com.neo.util.framework.rest.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import io.helidon.microprofile.tests.junit5.AddBean;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;


public abstract class AbstractIntegrationTest {

    protected JsonNode validateResponse(Response response, int code) {
        Assertions.assertEquals(code, response.getStatus());
        return JsonUtil.fromJson(response.readEntity(String.class));
    }
}
