package com.neo.util.helidon.rest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@HelidonTest
@AddBean(SecurityResource.class)
@AddBean(BasicAuthorizationProvider.class)
class SecurityIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;

    @Test
    void secureResourceTest() {
        //Arrange
        //Act
        Response response = webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_SECURED).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.NORMAL_TOKEN)
                .method("GET");
        //Assert
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(BasicAuthorizationProvider.NORMAL_PRINCIPAL.getName(), response.readEntity(String.class));
    }

    @Test
    void secureResourceFailTest() {
        //Arrange
        //Act
        Response response = webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_SECURED).request()
                .method("GET");
        //Assert
        Assertions.assertEquals(401, response.getStatus());
        JsonNode responseBody = JsonUtil.fromJson(response.readEntity(String.class)).get("error");
        Assertions.assertEquals("auth/000", responseBody.get("code").asText());
        Assertions.assertEquals("Unauthorized", responseBody.get("message").asText());
    }

    @Test
    void roleResourceTest() {
        //Arrange
        //Act
        Response response = webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_ROLE).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.ADMIN_TOKEN)
                .method("GET");
        //Assert
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(BasicAuthorizationProvider.ADMIN_PRINCIPAL.getName(),response.readEntity(String.class));
    }

    @Test
    void roleResourceFailureTest() {
        //Arrange
        //Act
        Response response = webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_ROLE).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.NORMAL_TOKEN)
                .method("GET");
        //Assert
        Assertions.assertEquals(403, response.getStatus());
        JsonNode responseBody = JsonUtil.fromJson(response.readEntity(String.class)).get("error");
        Assertions.assertEquals("auth/001", responseBody.get("code").asText());
        Assertions.assertEquals("Forbidden", responseBody.get("message").asText());
    }
}
