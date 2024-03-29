package com.neo.util.helidon.rest.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.impl.entity.AbstractEntityRestEndpoint;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@HelidonTest(resetPerTest = true)
abstract class AbstractEntityRestEndpointIT extends AbstractIntegrationTest {

    protected abstract JsonNode defaultJSONEntity();

    protected abstract JsonNode editedJSONEntity();

    protected abstract String resourceLocation();

    protected abstract Object getPrimaryKey();

    @Test
    @Order(0)
    void createEntityTest(WebTarget webTarget) {
        //Arrange
        Entity<String> content = Entity.entity(defaultJSONEntity().toString(), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("POST", content);

        //Assert
        validateResponse(response,200);
    }

    @Test
    @Order(1)
    void createExistingEntityTest(WebTarget webTarget) {
        //Arrange
        Entity<String> content = Entity.entity(defaultJSONEntity().toString(), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("POST", content);

        //Assert
        JsonNode responseBody = validateResponse(response,400);
        Assertions.assertEquals(AbstractEntityRestEndpoint.EX_ENTITY_NONE_UNIQUE.getExceptionId(), responseBody.get("code").asText());
    }

    @Test
    @Order(2)
    void retrieveEntityByIdTest(WebTarget webTarget) {
        //Arrange

        //Act
        Response response = webTarget.path(resourceLocation() + "/" + getPrimaryKey()).request()
                .method("GET");

        //Assert
        JsonNode responseBody = validateResponse(response,200);

        Assertions.assertEquals(getPrimaryKey(), responseBody.get("id").asInt());
    }

    @Test
    @Order(30)
    void editEntityTest(WebTarget webTarget) {
        //Arrange
        Entity<String> content = Entity.entity(editedJSONEntity().toString(), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("PUT", content);

        //Assert
        JsonNode responseBody = validateResponse(response,200);

        Assertions.assertEquals(editedJSONEntity().get("id").asInt(), responseBody.get("id").asInt());
    }

    @Test
    @Order(31)
    void deleteEntityTest(WebTarget webTarget) {
        //Arrange
        //Act
        Response response = webTarget.path(resourceLocation() + "/" + getPrimaryKey()).request()
                .method("DELETE");

        //Assert
        validateResponse(response,200);
    }

    @Test
    void createMissingFieldEntityTest(WebTarget webTarget) {
        //Arrange
        JsonNode entity = JsonUtil.emptyObjectNode();

        Entity<String> content = Entity.entity(JsonUtil.toJson(entity), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("POST", content);

        //Assert
        JsonNode responseBody = validateResponse(response,400);
        Assertions.assertEquals(AbstractEntityRestEndpoint.EX_ENTITY_MISSING_FIELDS.getExceptionId(), responseBody.get("code").asText());
    }

    @Test
    void deleteNonexistentEntityTest(WebTarget webTarget) {
        //Arrange
        //Act
        Response response = webTarget.path(resourceLocation() + "/" + "NonexistentEntity").request()
                .method("DELETE");

        //Assert
        Assertions.assertEquals(404, response.getStatus());
    }

    @Test
    void retrieveNoneExistentEntityByIdTest(WebTarget webTarget) {
        //Arrange
        //Act
        Response response = webTarget.path(resourceLocation() + "/" + "ENTITY").request()
                .method("GET");

        //Assert
        Assertions.assertEquals(404, response.getStatus());
    }
}
