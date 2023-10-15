package com.neo.util.helidon.rest.parser;

import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.testing.junit5.AddBean;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HelidonTest
@AddBean(OutboundJsonResource.class)
class OutboundJsonIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;

    @Test
    void basic() {
        //Arrange
        String expect = JsonUtil.toJson(OutboundJsonResource.OUTBOUND_DTO);

        //Act
        Response response = webTarget.path(OutboundJsonResource.RESOURCE_LOCATION + OutboundJsonResource.P_BASIC).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(expect, response.readEntity(String.class));
    }


    @Test
    void viewPublic() {
        //Arrange
        String expect = JsonUtil.toJson(OutboundJsonResource.OUTBOUND_DTO, Views.Public.class);

        //Act
        Response response = webTarget.path(OutboundJsonResource.RESOURCE_LOCATION + OutboundJsonResource.P_VIEW_PUBLIC).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(expect, response.readEntity(String.class));
    }


    @Test
    void viewsOwner() {
        //Arrange
        String expect = JsonUtil.toJson(OutboundJsonResource.OUTBOUND_DTO, Views.Owner.class);

        //Act
        Response response = webTarget.path(OutboundJsonResource.RESOURCE_LOCATION + OutboundJsonResource.P_VIEW_OWNER).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(expect, response.readEntity(String.class));
    }


    @Test
    void basicInternal() {
        //Arrange
        String expect = JsonUtil.toJson(OutboundJsonResource.OUTBOUND_DTO, Views.Internal.class);

        //Act
        Response response = webTarget.path(OutboundJsonResource.RESOURCE_LOCATION + OutboundJsonResource.P_BASIC).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(expect, response.readEntity(String.class));
    }


    @Test
    void outboundJsonParsingTest() {
        //Arrange
        String json = JsonUtil.toJson(OutboundJsonResource.OUTBOUND_JSON);

        //Act
        Response response = webTarget.path(OutboundJsonResource.RESOURCE_LOCATION + OutboundJsonResource.P_OUT_PARSING).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(json, response.readEntity(String.class));
    }
}
