package com.neo.util.helidon.rest.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import io.helidon.microprofile.testing.junit5.AddBean;
import io.helidon.microprofile.testing.junit5.HelidonTest;

@HelidonTest
@AddBean(TestPersonResource.class)
class EntityRestEndpointIT extends AbstractEntityRestEndpointIT {

    @Override
    protected JsonNode defaultJSONEntity() {
        ObjectNode json = JsonUtil.emptyObjectNode();
        json.put("name", "aName");
        json.put("description", "aDescription");
        json.put("age",2000);
        return json;
    }

    @Override
    protected JsonNode editedJSONEntity() {
        ObjectNode json = JsonUtil.emptyObjectNode();
        json.put("id", 1);
        json.put("name", "aNewName");
        json.put("description", "aNewDescription");
        json.put("age",2001);
        return json;
    }

    @Override
    protected String resourceLocation() {
        return TestPersonResource.RESOURCE_LOCATION;
    }

    @Override
    protected Object getPrimaryKey() {
        return 1;
    }
}
