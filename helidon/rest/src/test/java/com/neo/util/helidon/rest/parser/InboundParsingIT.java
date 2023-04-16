package com.neo.util.helidon.rest.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
//import com.neo.util.framework.rest.impl.parsing.AdvancedInboundDtoParser;
//import com.neo.util.framework.rest.impl.parsing.BasicInboundDtoParser;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HelidonTest
@AddBean(InboundParserResource.class)
//@AddBean(AdvancedInboundDtoParser.class)
//@AddBean(BasicInboundDtoParser.class)
class InboundParsingIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;


    @Test
    void basicParsingTest() {
        //Arrange
        InboundParserResource.BasicInboundDto basic = new InboundParserResource.BasicInboundDto(
                "AString",0
        );

        String json = JsonUtil.toJson(basic);

        Entity<String> content = Entity.entity(json, MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(InboundParserResource.RESOURCE_LOCATION + InboundParserResource.P_BASIC).request().method("POST",content);
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        String body = response.readEntity(String.class);
        Assertions.assertEquals(json, body);
    }

    @Test
    void basicMissingFieldTest() {
        //Arrange
        InboundParserResource.BasicInboundDto basic = new InboundParserResource.BasicInboundDto(
                "AString",0
        );

        ObjectNode json = JsonUtil.fromPojo(basic);
        json.remove("aString");

        Entity<String> content = Entity.entity(JsonUtil.toJson(json), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(InboundParserResource.RESOURCE_LOCATION + InboundParserResource.P_BASIC).request().method("POST",content);
        //Assert

        JsonNode body = validateResponse(response, 400);
        Assertions.assertEquals("$.aString", body.get("message").asText().substring(0,9));
    }

    @Test
    void advancedParsingTest() {
        //Arrange
        InboundParserResource.AdvancedInboundDto basic = new InboundParserResource.AdvancedInboundDto(
                "AString",true
        );

        String json = JsonUtil.toJson(basic);

        Entity<String> content = Entity.entity(json, MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(InboundParserResource.RESOURCE_LOCATION + InboundParserResource.P_ADVANCED).request().method("POST",content);
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        String body = response.readEntity(String.class);
        Assertions.assertEquals(json, body);
    }

    @Test
    void advancedMissingFieldTest() {
        //Arrange
        InboundParserResource.AdvancedInboundDto advancedInboundDto = new InboundParserResource.AdvancedInboundDto(
                "AString",true
        );

        ObjectNode json = JsonUtil.fromPojo(advancedInboundDto);
        json.remove("a_different_name");

        Entity<String> content = Entity.entity(JsonUtil.toJson(json), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(InboundParserResource.RESOURCE_LOCATION + InboundParserResource.P_ADVANCED).request().method("POST",content);
        //Assert

        JsonNode body = validateResponse(response, 400);
        Assertions.assertEquals("$.a_different_name", body.get("message").asText().substring(0,18));
    }
}
