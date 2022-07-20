package com.neo.util.helidon.rest.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class AbstractEntityRestEndpointIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;

    protected abstract JsonNode defaultJSONEntity();

    protected abstract JsonNode editedJSONEntity();

    protected abstract String resourceLocation();

    protected abstract Object getPrimaryKey();

    @Test
    @Order(0)
    void createEntityTest() {
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
    void createExistingEntityTest() {
        //Arrange
        Entity<String> content = Entity.entity(defaultJSONEntity().toString(), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("POST", content);

        //Assert
        JsonNode responseBody = validateResponse(response,400);
        Assertions.assertEquals("resources/003", responseBody.get("error").get("code").asText());
    }

    @Test
    @Order(2)
    void retrieveEntityByIdTest() {
        //Arrange

        //Act
        Response response = webTarget.path(resourceLocation() + "/" + getPrimaryKey()).request()
                .method("GET");

        //Assert
        JsonNode responseBody = validateResponse(response,200);
        JsonNode responseEntity = responseBody.get("data");

        Assertions.assertEquals(getPrimaryKey(), responseEntity.get("id").asInt());
    }

    @Test
    @Order(30)
    void editEntityTest() {
        //Arrange
        Entity<String> content = Entity.entity(editedJSONEntity().toString(), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("PUT", content);

        //Assert
        JsonNode responseBody = validateResponse(response,200);

        JsonNode responseEntity = responseBody.get("data");

        Assertions.assertEquals(editedJSONEntity().get("id").asInt(), responseEntity.get("id").asInt());
    }

    @Test
    @Order(31)
    void deleteEntityTest() {
        //Arrange
        //Act
        Response response = webTarget.path(resourceLocation() + "/" + getPrimaryKey()).request()
                .method("DELETE");

        //Assert
        validateResponse(response,200);
    }

    @Test
    void createMissingFieldEntityTest() {
        //Arrange
        JsonNode entity = JsonUtil.emptyObjectNode();

        Entity<String> content = Entity.entity(JsonUtil.toJson(entity), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(resourceLocation()).request()
                .method("POST", content);

        //Assert
        JsonNode responseBody = validateResponse(response,400);
        Assertions.assertEquals("resources/002", responseBody.get("error").get("code").asText());
    }

    @Test
    void deleteNonexistentEntityTest() {
        //Arrange
        //Act
        Response response = webTarget.path(resourceLocation() + "/" + "NonexistentEntity").request()
                .method("DELETE");

        //Assert
        Assertions.assertEquals(404, response.getStatus());
    }

    @Test
    void retrieveNoneExistentEntityByIdTest() {
        //Arrange
        //Act
        Response response = webTarget.path(resourceLocation() + "/" + "ENTITY").request()
                .method("GET");

        //Assert
        Assertions.assertEquals(404, response.getStatus());
    }
}
