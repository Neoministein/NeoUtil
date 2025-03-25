package com.neo.util.framework.impl.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestMapping {

    @Test
    void test() {

        MappingSchema mappingSchema = new MappingSchema(format, jsonSchema);

        MappingObject mappingObject = new MappingObject(format);
        MappingObjectv2 mappingObjectv2 = new MappingObjectv2(mappingSchema);

        JsonNode jsonNode1 = mappingObjectv2.transformJson((ObjectNode) JsonUtil.fromJson(json));
        System.out.println(JsonUtil.toPrettyJson(jsonNode1));
        Assertions.assertEquals("{\"owner\":\"CustomerName\",\"asnType\":\"Receipt\",\"transportUnit\":{\"tuId\":\"T-10001\",\"tuType\":\"PALLET\"},\"asnLine\":[{\"lineNumber\":0,\"productId\":\"iPhone\",\"productVersionId\":\"default\",\"packagingUom\":\"CASE\",\"quantityExpected\":\"10.0\",\"attributeValue\":[{\"name\":\"lotcode\",\"value\":\"LOT-120\"},{\"name\":\"checkInDate\",\"value\":\"2025-03-11\"}]}]}", JsonUtil.toJson(jsonNode1));


        JsonNode jsonNode = mappingObject.transformJson((ObjectNode) JsonUtil.fromJson(json));
        System.out.println(JsonUtil.toPrettyJson(jsonNode));
        Assertions.assertEquals("{\"root\":{\"owner\":\"CustomerName\",\"asnType\":\"Receipt\",\"transportUnit\":{\"tuId\":\"T-10001\",\"tuType\":\"PALLET\"},\"asnLine\":[{\"lineNumber\":0,\"productId\":\"iPhone\",\"productVersionId\":\"default\",\"packagingUom\":\"CASE\",\"quantityExpected\":\"10.0\",\"attributeValue\":[{\"name\":\"lotcode\",\"value\":\"LOT-120\"},{\"name\":\"checkInDate\",\"value\":\"2025-03-11\"}]}]}}", JsonUtil.toJson(jsonNode));
    }

    private static final String json = "{\n" +
            "            \"ZEWM_E1LTORH\": {\n" +
            "                \"LGNUM\": \"CustomerName\",\n" +
            "                \"WHO\": \"T-10001\",\n" +
            "                \"TART\": \"\"\n" +
            "            },\n" +
            "            \"ZEWM_E1LTORI\": [\n" +
            "                {\n" +
            "                    \"MATNR\": \"iPhone\",\n" +
            "                    \"VSOLM\": \"10.0\",\n" +
            "                    \"MEINS\": \"CASE\",\n" +
            "                    \"WDATU\": \"2025-03-11\",\n" +
            "                    \"WENUM\": \"LOT-120\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }";

    private static final String jsonSchema = "{\n" +
            "\t\"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"ZEWM_E1LTORH\": {\n" +
            "\t\t\t\"type\": \"object\",\n" +
            "\t\t\t\"properties\": {\n" +
            "\t\t\t\t\"LGNUM\": {\n" +
            "\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"WHO\": {\n" +
            "\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"TART\": {\n" +
            "\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"required\": [\n" +
            "\t\t\t\t\"LGNUM\",\n" +
            "\t\t\t\t\"WHO\",\n" +
            "\t\t\t\t\"TART\"\n" +
            "\t\t\t]\n" +
            "\t\t},\n" +
            "\t\t\"ZEWM_E1LTORI\": {\n" +
            "\t\t\t\"type\": \"array\",\n" +
            "\t\t\t\"items\": {\n" +
            "\t\t\t\t\"type\": \"object\",\n" +
            "\t\t\t\t\"properties\": {\n" +
            "\t\t\t\t\t\"MATNR\": {\n" +
            "\t\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"VSOLM\": {\n" +
            "\t\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"MEINS\": {\n" +
            "\t\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"WDATU\": {\n" +
            "\t\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"WENUM\": {\n" +
            "\t\t\t\t\t\t\"type\": \"string\"\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"required\": [\n" +
            "\t\t\t\t\t\"MATNR\",\n" +
            "\t\t\t\t\t\"VSOLM\",\n" +
            "\t\t\t\t\t\"MEINS\",\n" +
            "\t\t\t\t\t\"WDATU\",\n" +
            "\t\t\t\t\t\"WENUM\"\n" +
            "\t\t\t\t]\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\n" +
            "\t\t\"ZEWM_E1LTORH\",\n" +
            "\t\t\"ZEWM_E1LTORI\"\n" +
            "\t]\n" +
            "}";

    private static final String format = "" +
            "<root type=\"object\">" +
            "<owner type=\"String\" value=\"#{ZEWM_E1LTORH.LGNUM}\"/>\n" +
            "<asnType type=\"String\" value=\"Receipt\"/>\n" +
            "<transportUnit type=\"object\">\n" +
            "<tuId type=\"String\" value=\"#{ZEWM_E1LTORH.WHO}\"/>\n" +
            "<tuType type=\"String\" value=\"PALLET\"/>\n" +
            "</transportUnit>\n" +
            "<asnLine type=\"array\" var=\"asnline\" loop=\"#{ZEWM_E1LTORI}\">\n" +
            "<lineNumber type=\"Integer\" value=\"#{asnline._iteration}\"/>\n" +
            "<productId type=\"String\" value=\"#{asnline.MATNR}\"/>\n" +
            "<productVersionId type=\"String\" value=\"default\"/>\n" +
            "<packagingUom type=\"String\" value=\"#{asnline.MEINS}\"/>\n" +
            "<quantityExpected type=\"String\" value=\"#{asnline.VSOLM}\"/>\n" +
            "<attributeValue type=\"array\">\n" +
            "<name type=\"String\" value=\"lotcode\"/>\n" +
            "<value type=\"String\" value=\"#{asnline.WENUM}\"/>\n" +
            "</attributeValue>\n" +
            "<attributeValue type=\"array\">\n" +
            "<name type=\"String\" value=\"checkInDate\"/>\n" +
            "<value type=\"String\" value=\"#{asnline.WDATU}\"/>\n" +
            "</attributeValue>\n" +
            "</asnLine>\n" +
            "</root>";
}
