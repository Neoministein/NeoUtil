package com.neo.util.framework.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.impl.ElExpressionHandler;
import com.neo.util.framework.mapping.impl.MappingELResolver;
import com.neo.util.framework.mapping.impl.MappingObjectv2;
import com.neo.util.framework.mapping.impl.MappingStateHolder;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.spi.BeanManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJunit5Extension.class)
public class ExpressionTest {


    @WeldSetup
    protected WeldInitiator weld = WeldInitiator.from(new Weld()).build();

    @Test
    void applicationEventTest() {
        BeanManager beanManager = weld.select(BeanManager.class).get();
        //MathBean mathBean = weld.select(MathBean.class).get();
        RequestContextController rcc = weld.select(RequestContextController.class).get();

        ExpressionFactory factory = ExpressionFactory.newInstance();
        StandardELContext context = new StandardELContext(factory);
        context.putContext(BeanManager.class, beanManager);
        context.addELResolver(new MappingELResolver(beanManager));


        MappingSchema mappingSchema = new MappingSchema(new ElExpressionHandler(context), format, jsonSchema);
        ValueExpression expression = factory.createValueExpression(context, "#{mathBean.a.idk}", JsonNode.class);
        ValueExpression expression2 = factory.createValueExpression(context, "IDK", String.class);

        Object o = null;
        rcc.activate();

        weld.select(MappingStateHolder.class).get().setSchema(mappingSchema, null);
        o = expression.getValue(context);
        MappingObjectv2 objectv2 = new MappingObjectv2(mappingSchema);
        objectv2.transformJson((ObjectNode) JsonUtil.fromJson(json));
        System.out.println(o); // Output: 11
        rcc.deactivate();

        rcc.activate();
        weld.select(MappingStateHolder.class).get().setSchema(mappingSchema, null);
        o = expression.getValue(context);
        System.out.println(o); // Output: 11
        rcc.deactivate();
        System.out.println();
    }

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
}
