package com.neo.util.framework.mapping;

import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.impl.MappingSchemaFactory;
import com.neo.util.framework.mapping.impl.MappingServiceImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(WeldJunit5Extension.class)
class ExpressionTest {

    @WeldSetup
    protected WeldInitiator weld = WeldInitiator.from(new Weld()).build();

    protected MappingServiceImpl mappingService;

    protected ExpressionHandler expressionHandler;

    protected MappingSchemaFactory mappingSchemaFactory;

    @BeforeEach
    void before() {
        this.mappingService = weld.select(MappingServiceImpl.class).get();
        this.expressionHandler = weld.select(ExpressionHandler.class).get();
        this.mappingSchemaFactory = weld.select(MappingSchemaFactory.class).get();
    }

    @Test
    void allSchemaTests() throws IOException {
        File[] tests = ResourceUtil.getFolderContent("./tests");

        for (File testLocation: tests) {
            String input = Files.readString(Path.of(testLocation.getPath() + "\\input.json"));
            String jsonSchema = Files.readString(Path.of(testLocation.getPath() + "\\json-schema.json"));
            String mappingSchema = Files.readString(Path.of(testLocation.getPath() + "\\mapping-schema.xml"));
            String output = Files.readString(Path.of(testLocation.getPath() + "\\output.json"));

            validateSchemaTest(testLocation.getName(), input, jsonSchema, mappingSchema, output);
        }
    }

    @Test
    void singleSchemaTest() throws IOException {
        File testLocation = ResourceUtil.getFolderContent("./tests/defaultValue")[0].getParentFile();

        String input = Files.readString(Path.of(testLocation.getPath() + "\\input.json"));
        String jsonSchema = Files.readString(Path.of(testLocation.getPath() + "\\json-schema.json"));
        String mappingSchema = Files.readString(Path.of(testLocation.getPath() + "\\mapping-schema.xml"));
        String output = Files.readString(Path.of(testLocation.getPath() + "\\output.json"));

        validateSchemaTest(testLocation.getName(), input, jsonSchema, mappingSchema, output);
    }

    void validateSchemaTest(String name, String input, String schema, String mappingSchemaXml, String output) {
        MappingSchema mappingSchema = mappingSchemaFactory.createSchema(name, mappingSchemaXml, schema);

        JsonNode result = mappingService.transformJson(mappingSchema, (ObjectNode) JsonUtil.fromJson(input));

        Assertions.assertEquals(JsonUtil.fromJson(output).toPrettyString(), result.toPrettyString(), "The Test [" + name + "] Failed" );
    }
}
