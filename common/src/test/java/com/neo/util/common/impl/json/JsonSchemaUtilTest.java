
package com.neo.util.common.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.ResourceUtil;
import com.networknt.schema.JsonSchema;
import org.junit.jupiter.api.Test;

class JsonSchemaUtilTest {

    @Test
    void javaPojoToJsonTest() {
        JsonSchema jsonSchema = JsonSchemaUtil.generateNewSchema(ResourceUtil.getResourceFileAsString("schema/MatchState.json"));
        JsonNode node = JsonSchemaUtil.generateSampleJson(jsonSchema);
        JsonNode map = node.get("map");
        System.out.println(node.toPrettyString());
        jsonSchema.initializeValidators();
        //Map<String, JsonDataType> values = JsonSchemaUtil.extractAllFields(jsonSchema);
        //System.out.println(values);
    }

}
