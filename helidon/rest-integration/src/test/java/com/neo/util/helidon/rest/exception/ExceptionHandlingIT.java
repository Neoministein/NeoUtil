package com.neo.util.helidon.rest.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@HelidonTest
@AddBean(ExceptionResource.class)
class ExceptionHandlingIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;

    @Test
    void success() {
        //Arrange
        //Act
        Response response = webTarget.path(ExceptionResource.RESOURCE_LOCATION + ExceptionResource.P_SUCCESS).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    void runtimeException() {
        //Arrange
        //Act
        Response response = webTarget.path(ExceptionResource.RESOURCE_LOCATION + ExceptionResource.P_RUNTIME).request().method("GET");
        //Assert

        Assertions.assertEquals(500, response.getStatus());
        validateErrorObject(
                JsonUtil.fromJson(response.readEntity(String.class)),
                "unknown",
                "Internal server error please try again later"
        );
    }

    @Test
    void internalLogic() {
        //Arrange
        //Act
        Response response = webTarget.path(ExceptionResource.RESOURCE_LOCATION + ExceptionResource.P_INTERNAL_LOGIC).request().method("GET");
        //Assert

        Assertions.assertEquals(500, response.getStatus());
        validateErrorObject(
                JsonUtil.fromJson(response.readEntity(String.class)),
                "unknown",
                "Internal server error please try again later"
        );
    }

    @Test
    void internalJson() {
        //Arrange
        //Act
        Response response = webTarget.path(ExceptionResource.RESOURCE_LOCATION + ExceptionResource.P_INTERNAL_JSON).request().method("GET");
        //Assert

        Assertions.assertEquals(400, response.getStatus());
        validateErrorObject(
                JsonUtil.fromJson(response.readEntity(String.class)),
                "json/000",
                "Invalid json format in the request body: Test Internal Json Exception"
        );
    }

    @Test
    void notFound() {
        //Arrange
        String responseMessage = "No handler found for path: " + ExceptionResource.RESOURCE_LOCATION + "/notFound";

        //Act
        Response response = webTarget.path(ExceptionResource.RESOURCE_LOCATION + "/notFound").request().method("GET");
        //Assert

        Assertions.assertEquals(404, response.getStatus());
        Assertions.assertEquals(responseMessage, response.readEntity(String.class));
    }

    @Test
    void invalidMethod() {
        //Arrange
        //Act
        Response response = webTarget.path(ExceptionResource.RESOURCE_LOCATION + ExceptionResource.P_SUCCESS ).request().method("POST");
        //Assert

        Assertions.assertEquals(405, response.getStatus());
    }

    protected void validateErrorObject(JsonNode responseObject, String errorCode, String errorMessage) {
        JsonNode error = responseObject.get("error");
        Assertions.assertEquals(errorCode, error.get("code").asText());
        Assertions.assertEquals(errorMessage, error.get("message").asText());
    }
}
