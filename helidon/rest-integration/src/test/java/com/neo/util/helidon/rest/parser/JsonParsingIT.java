package com.neo.util.helidon.rest.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@HelidonTest
@AddBean(JsonEndpointResource.class)
class JsonParsingIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;

    @Test
    void jsonParseTest() {
        //Arrange
        JsonNode json = JsonUtil.fromJson(RANDOM_JSON);

        Entity<String> content = Entity.entity(JsonUtil.toJson(json), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(JsonEndpointResource.RESOURCE_LOCATION + JsonEndpointResource.P_PARSING).request().method("POST",content);
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(JsonUtil.toJson(json), response.readEntity(String.class));
    }

    @Test
    void jsonSchemaValidationTest() {
        //Arrange
        ObjectNode jsonBody = JsonUtil.emptyObjectNode();
        jsonBody.put("string","AString")
                .put("integer",1000)
                .put("boolean",true);

        Entity<String> content = Entity.entity(JsonUtil.toJson(jsonBody), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(JsonEndpointResource.RESOURCE_LOCATION + JsonEndpointResource.P_PARSING).request().method("POST",content);
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(JsonUtil.toJson(jsonBody), response.readEntity(String.class));
    }

    @Test
    void jsonSchemaInvalidationTest() {
        //Arrange
        ObjectNode jsonBody = JsonUtil.emptyObjectNode();
        jsonBody.put("string","AString")
                .put("integer",1000);

        Entity<String> content = Entity.entity(JsonUtil.toJson(jsonBody), MediaType.APPLICATION_JSON_TYPE);
        //Act
        Response response = webTarget.path(JsonEndpointResource.RESOURCE_LOCATION + JsonEndpointResource.P_SCHEMA).request().method("POST",content);
        //Assert

        Assertions.assertEquals(400, response.getStatus());
        JsonNode responseBoy = JsonUtil.fromJson(response.readEntity(String.class));
        Assertions.assertEquals("Invalid json format in the request body: $.boolean: is missing but it is required", responseBoy.get("error").get("message").asText());
    }

    protected static final String RANDOM_JSON = "[\n" + "  {\n" + "    \"_id\": \"62d3ec24d160c5a72a37ccf2\",\n"
            + "    \"index\": 0,\n" + "    \"guid\": \"f20942f5-e953-45eb-b835-e485fd7387dc\",\n"
            + "    \"isActive\": true,\n" + "    \"balance\": \"$1,275.96\",\n"
            + "    \"picture\": \"http://placehold.it/32x32\",\n" + "    \"age\": 31,\n"
            + "    \"eyeColor\": \"blue\",\n" + "    \"name\": \"Peterson Kim\",\n" + "    \"gender\": \"male\",\n"
            + "    \"company\": \"INQUALA\",\n" + "    \"email\": \"petersonkim@inquala.com\",\n"
            + "    \"phone\": \"+1 (957) 597-2576\",\n"
            + "    \"address\": \"534 Frost Street, Camino, Massachusetts, 8904\",\n"
            + "    \"about\": \"Minim nulla in officia non ea sint do pariatur reprehenderit. Exercitation est voluptate tempor deserunt mollit fugiat minim aute. Duis consectetur eu do duis voluptate elit ex eiusmod adipisicing. Do deserunt cillum mollit sint commodo consequat ex et. Consectetur velit laboris labore elit laborum ullamco dolore. Mollit cillum dolore Lorem mollit. Pariatur nostrud enim magna enim qui Lorem mollit irure magna eu adipisicing ut minim.\\r\\n\",\n"
            + "    \"registered\": \"2019-03-01T09:33:57 -01:00\",\n" + "    \"latitude\": -32.430624,\n"
            + "    \"longitude\": 105.152207,\n" + "    \"tags\": [\n" + "      \"ipsum\",\n" + "      \"officia\",\n"
            + "      \"labore\",\n" + "      \"nostrud\",\n" + "      \"ea\",\n" + "      \"pariatur\",\n"
            + "      \"sint\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n" + "        \"id\": 0,\n"
            + "        \"name\": \"Hill Clayton\"\n" + "      },\n" + "      {\n" + "        \"id\": 1,\n"
            + "        \"name\": \"Sharon Morse\"\n" + "      },\n" + "      {\n" + "        \"id\": 2,\n"
            + "        \"name\": \"Barnes Dyer\"\n" + "      }\n" + "    ],\n"
            + "    \"greeting\": \"Hello, Peterson Kim! You have 2 unread messages.\",\n"
            + "    \"favoriteFruit\": \"apple\"\n" + "  },\n" + "  {\n" + "    \"_id\": \"62d3ec242c07516271d44f45\",\n"
            + "    \"index\": 1,\n" + "    \"guid\": \"c333ecb4-41f4-4a47-a8ba-bc09115edd14\",\n"
            + "    \"isActive\": true,\n" + "    \"balance\": \"$1,590.03\",\n"
            + "    \"picture\": \"http://placehold.it/32x32\",\n" + "    \"age\": 39,\n"
            + "    \"eyeColor\": \"green\",\n" + "    \"name\": \"Chris Lancaster\",\n"
            + "    \"gender\": \"female\",\n" + "    \"company\": \"BIOLIVE\",\n"
            + "    \"email\": \"chrislancaster@biolive.com\",\n" + "    \"phone\": \"+1 (954) 451-3642\",\n"
            + "    \"address\": \"898 Richardson Street, Indio, Rhode Island, 1733\",\n"
            + "    \"about\": \"Labore tempor est aliqua sit commodo consequat consequat ut excepteur. Pariatur minim nostrud adipisicing sit fugiat nostrud minim consequat ipsum dolor sint sint incididunt nulla. Consequat enim nostrud cillum aliquip laborum enim. Anim id exercitation duis amet.\\r\\n\",\n"
            + "    \"registered\": \"2021-10-25T12:21:17 -02:00\",\n" + "    \"latitude\": -27.540073,\n"
            + "    \"longitude\": 163.848436,\n" + "    \"tags\": [\n" + "      \"quis\",\n" + "      \"ullamco\",\n"
            + "      \"ut\",\n" + "      \"cupidatat\",\n" + "      \"consectetur\",\n" + "      \"laborum\",\n"
            + "      \"ut\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n" + "        \"id\": 0,\n"
            + "        \"name\": \"Bonnie Daniels\"\n" + "      },\n" + "      {\n" + "        \"id\": 1,\n"
            + "        \"name\": \"Pennington Evans\"\n" + "      },\n" + "      {\n" + "        \"id\": 2,\n"
            + "        \"name\": \"Flowers Webster\"\n" + "      }\n" + "    ],\n"
            + "    \"greeting\": \"Hello, Chris Lancaster! You have 10 unread messages.\",\n"
            + "    \"favoriteFruit\": \"apple\"\n" + "  },\n" + "  {\n" + "    \"_id\": \"62d3ec24b3d91d9ae954e392\",\n"
            + "    \"index\": 2,\n" + "    \"guid\": \"ca17a7f8-54a2-4cc3-8998-138cd5c24a7c\",\n"
            + "    \"isActive\": true,\n" + "    \"balance\": \"$2,872.92\",\n"
            + "    \"picture\": \"http://placehold.it/32x32\",\n" + "    \"age\": 34,\n"
            + "    \"eyeColor\": \"green\",\n" + "    \"name\": \"Freeman Ramirez\",\n" + "    \"gender\": \"male\",\n"
            + "    \"company\": \"MITROC\",\n" + "    \"email\": \"freemanramirez@mitroc.com\",\n"
            + "    \"phone\": \"+1 (948) 600-3790\",\n"
            + "    \"address\": \"806 Visitation Place, Alden, Connecticut, 979\",\n"
            + "    \"about\": \"Exercitation laboris occaecat eu duis aute nostrud velit magna laboris commodo sit ea dolore. Amet sunt velit anim dolor in nulla laborum eiusmod minim non ea. In cupidatat aute eiusmod proident reprehenderit qui ex. Reprehenderit sunt labore culpa aliquip aute.\\r\\n\",\n"
            + "    \"registered\": \"2014-02-07T04:29:00 -01:00\",\n" + "    \"latitude\": 76.164929,\n"
            + "    \"longitude\": 46.638821,\n" + "    \"tags\": [\n" + "      \"id\",\n" + "      \"aute\",\n"
            + "      \"consectetur\",\n" + "      \"do\",\n" + "      \"et\",\n" + "      \"elit\",\n"
            + "      \"enim\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n" + "        \"id\": 0,\n"
            + "        \"name\": \"Lula Bernard\"\n" + "      },\n" + "      {\n" + "        \"id\": 1,\n"
            + "        \"name\": \"Reba Buchanan\"\n" + "      },\n" + "      {\n" + "        \"id\": 2,\n"
            + "        \"name\": \"Mclaughlin Fry\"\n" + "      }\n" + "    ],\n"
            + "    \"greeting\": \"Hello, Freeman Ramirez! You have 2 unread messages.\",\n"
            + "    \"favoriteFruit\": \"strawberry\"\n" + "  },\n" + "  {\n"
            + "    \"_id\": \"62d3ec24f0911443f63b70c6\",\n" + "    \"index\": 3,\n"
            + "    \"guid\": \"52dfa24a-f875-4a2e-a58a-113148ace070\",\n" + "    \"isActive\": true,\n"
            + "    \"balance\": \"$2,659.16\",\n" + "    \"picture\": \"http://placehold.it/32x32\",\n"
            + "    \"age\": 29,\n" + "    \"eyeColor\": \"brown\",\n" + "    \"name\": \"Bennett Collins\",\n"
            + "    \"gender\": \"male\",\n" + "    \"company\": \"CENTREE\",\n"
            + "    \"email\": \"bennettcollins@centree.com\",\n" + "    \"phone\": \"+1 (936) 488-3978\",\n"
            + "    \"address\": \"109 Campus Road, Takilma, Palau, 6816\",\n"
            + "    \"about\": \"In voluptate elit anim reprehenderit non enim reprehenderit cillum. Fugiat magna proident culpa ea Lorem dolore. Consequat non ea esse duis in sint exercitation deserunt dolor ullamco excepteur commodo ea Lorem. Incididunt proident sint voluptate fugiat irure qui elit cupidatat veniam sit velit sint qui. Anim fugiat irure voluptate ipsum laborum est magna dolor amet nostrud. Pariatur in qui laborum mollit non non mollit esse dolore incididunt.\\r\\n\",\n"
            + "    \"registered\": \"2021-05-08T02:27:56 -02:00\",\n" + "    \"latitude\": 67.767507,\n"
            + "    \"longitude\": -116.733425,\n" + "    \"tags\": [\n" + "      \"aliqua\",\n" + "      \"ut\",\n"
            + "      \"ad\",\n" + "      \"tempor\",\n" + "      \"Lorem\",\n" + "      \"commodo\",\n"
            + "      \"consequat\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n" + "        \"id\": 0,\n"
            + "        \"name\": \"Reid Best\"\n" + "      },\n" + "      {\n" + "        \"id\": 1,\n"
            + "        \"name\": \"Knox Shelton\"\n" + "      },\n" + "      {\n" + "        \"id\": 2,\n"
            + "        \"name\": \"Esmeralda Hodges\"\n" + "      }\n" + "    ],\n"
            + "    \"greeting\": \"Hello, Bennett Collins! You have 2 unread messages.\",\n"
            + "    \"favoriteFruit\": \"banana\"\n" + "  },\n" + "  {\n"
            + "    \"_id\": \"62d3ec2429e718e9363f8ab7\",\n" + "    \"index\": 4,\n"
            + "    \"guid\": \"d6956330-8564-4289-9364-e77d3f5d5bbd\",\n" + "    \"isActive\": false,\n"
            + "    \"balance\": \"$1,319.82\",\n" + "    \"picture\": \"http://placehold.it/32x32\",\n"
            + "    \"age\": 23,\n" + "    \"eyeColor\": \"green\",\n" + "    \"name\": \"Harvey Jefferson\",\n"
            + "    \"gender\": \"male\",\n" + "    \"company\": \"LEXICONDO\",\n"
            + "    \"email\": \"harveyjefferson@lexicondo.com\",\n" + "    \"phone\": \"+1 (998) 532-3282\",\n"
            + "    \"address\": \"647 Balfour Place, Matthews, Kentucky, 5833\",\n"
            + "    \"about\": \"Amet consectetur nulla mollit labore. Veniam minim enim et anim ipsum. Culpa enim adipisicing qui laboris anim Lorem proident occaecat laboris. Et cupidatat amet culpa sunt.\\r\\n\",\n"
            + "    \"registered\": \"2018-05-03T04:15:37 -02:00\",\n" + "    \"latitude\": 28.907278,\n"
            + "    \"longitude\": 78.408592,\n" + "    \"tags\": [\n" + "      \"sunt\",\n" + "      \"tempor\",\n"
            + "      \"culpa\",\n" + "      \"pariatur\",\n" + "      \"consectetur\",\n" + "      \"tempor\",\n"
            + "      \"id\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n" + "        \"id\": 0,\n"
            + "        \"name\": \"Brandi Yates\"\n" + "      },\n" + "      {\n" + "        \"id\": 1,\n"
            + "        \"name\": \"Tasha Snyder\"\n" + "      },\n" + "      {\n" + "        \"id\": 2,\n"
            + "        \"name\": \"Mia Cox\"\n" + "      }\n" + "    ],\n"
            + "    \"greeting\": \"Hello, Harvey Jefferson! You have 3 unread messages.\",\n"
            + "    \"favoriteFruit\": \"apple\"\n" + "  },\n" + "  {\n" + "    \"_id\": \"62d3ec24b63d45f648746d16\",\n"
            + "    \"index\": 5,\n" + "    \"guid\": \"5e3d6486-8d05-4567-b2e6-46710fc4347d\",\n"
            + "    \"isActive\": false,\n" + "    \"balance\": \"$3,269.58\",\n"
            + "    \"picture\": \"http://placehold.it/32x32\",\n" + "    \"age\": 39,\n"
            + "    \"eyeColor\": \"brown\",\n" + "    \"name\": \"David Higgins\",\n" + "    \"gender\": \"male\",\n"
            + "    \"company\": \"FROLIX\",\n" + "    \"email\": \"davidhiggins@frolix.com\",\n"
            + "    \"phone\": \"+1 (891) 599-2756\",\n"
            + "    \"address\": \"447 Kenmore Court, Grimsley, Idaho, 6599\",\n"
            + "    \"about\": \"Pariatur Lorem et et anim minim sint. Enim consequat incididunt veniam veniam sint et ullamco nisi in ullamco. Reprehenderit enim dolor velit aute aute. Amet et laboris laborum nulla officia ut magna incididunt laboris commodo ipsum. Voluptate occaecat mollit adipisicing amet id magna sunt tempor. Reprehenderit officia deserunt culpa dolore aliquip laboris qui aliquip. Sint voluptate nostrud velit do qui excepteur non.\\r\\n\",\n"
            + "    \"registered\": \"2014-04-17T01:52:00 -02:00\",\n" + "    \"latitude\": 10.629749,\n"
            + "    \"longitude\": -14.920938,\n" + "    \"tags\": [\n" + "      \"deserunt\",\n" + "      \"non\",\n"
            + "      \"occaecat\",\n" + "      \"nostrud\",\n" + "      \"culpa\",\n" + "      \"nostrud\",\n"
            + "      \"est\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n" + "        \"id\": 0,\n"
            + "        \"name\": \"Hester Ewing\"\n" + "      },\n" + "      {\n" + "        \"id\": 1,\n"
            + "        \"name\": \"Juliette Lang\"\n" + "      },\n" + "      {\n" + "        \"id\": 2,\n"
            + "        \"name\": \"Alba Torres\"\n" + "      }\n" + "    ],\n"
            + "    \"greeting\": \"Hello, David Higgins! You have 10 unread messages.\",\n"
            + "    \"favoriteFruit\": \"banana\"\n" + "  }\n" + "]";
}
