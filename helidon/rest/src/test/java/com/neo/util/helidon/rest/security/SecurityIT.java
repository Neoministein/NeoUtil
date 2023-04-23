package com.neo.util.helidon.rest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@HelidonTest(resetPerTest = true)
class SecurityIT extends AbstractIntegrationTest {

    @Test
    void secureResourceTest(WebTarget webTarget) {
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
    void secureResourceFailTest(WebTarget webTarget) {
        //Arrange
        //Act
        Response response = webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_SECURED).request()
                .method("GET");
        //Assert
        Assertions.assertEquals(401, response.getStatus());
        JsonNode responseBody = JsonUtil.fromJson(response.readEntity(String.class));
        Assertions.assertEquals(FrameworkConstants.EX_UNAUTHORIZED.getExceptionId(), responseBody.get("code").asText());
        Assertions.assertEquals(FrameworkConstants.EX_UNAUTHORIZED.getFormat(), responseBody.get("message").asText());
    }

    @Test
    void roleResourceTest(WebTarget webTarget) {
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
    void roleResourceFailureTest(WebTarget webTarget) {
        //Arrange
        //Act
        Response response = webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_ROLE).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.NORMAL_TOKEN)
                .method("GET");
        //Assert
        Assertions.assertEquals(403, response.getStatus());
        JsonNode responseBody = JsonUtil.fromJson(response.readEntity(String.class));
        Assertions.assertEquals(FrameworkConstants.EX_FORBIDDEN.getExceptionId(), responseBody.get("code").asText());
        Assertions.assertEquals(FrameworkConstants.EX_FORBIDDEN.getFormat(), responseBody.get("message").asText());
    }
}
