package com.neo.util.framework.impl.mapping;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ExpressExtractorTest {

    @Test
    void extractorTest() {
        List<ExpressExtractor.Value> value = null;



        value = ExpressExtractor.extractParts("#{value}");
        assertResult(0, ExpressExtractor.Type.EXPRESSION, "value", value);

        value = ExpressExtractor.extractParts(" #{value} ");
        assertResult(0, ExpressExtractor.Type.STATIC,       " ",    value);
        assertResult(1, ExpressExtractor.Type.EXPRESSION,   "value",value);
        assertResult(2, ExpressExtractor.Type.STATIC,       " ",    value);

        value = ExpressExtractor.extractParts("#{valueA}#{valueB}#{valueC}");
        assertResult(0, ExpressExtractor.Type.EXPRESSION,"valueA",    value);
        assertResult(1, ExpressExtractor.Type.EXPRESSION,"valueB",    value);
        assertResult(2, ExpressExtractor.Type.EXPRESSION,"valueC",    value);

        value = ExpressExtractor.extractParts(" #{valueA} #{valueB} #{valueC} ");
        assertResult(0, ExpressExtractor.Type.STATIC,       " ",    value);
        assertResult(1, ExpressExtractor.Type.EXPRESSION,"valueA",    value);
        assertResult(2, ExpressExtractor.Type.STATIC,       " ",    value);
        assertResult(3, ExpressExtractor.Type.EXPRESSION,"valueB",    value);
        assertResult(4, ExpressExtractor.Type.STATIC,       " ",    value);
        assertResult(5, ExpressExtractor.Type.EXPRESSION,"valueC",    value);
        assertResult(6, ExpressExtractor.Type.STATIC,       " ",    value);

        value = ExpressExtractor.extractParts("#{}");
        assertResult(0, ExpressExtractor.Type.EXPRESSION,"",    value);

        value = ExpressExtractor.extractParts("#{ }");
        assertResult(0, ExpressExtractor.Type.EXPRESSION," ",    value);

        value = ExpressExtractor.extractParts("#{ root.value }");
        assertResult(0, ExpressExtractor.Type.EXPRESSION," root.value ",    value);

        value = ExpressExtractor.extractParts("#{ root.value1 + root.value2 }");
        assertResult(0, ExpressExtractor.Type.EXPRESSION," root.value1 + root.value2 ",    value);

        value = ExpressExtractor.extractParts("#{ 6 + 5 }");
        assertResult(0, ExpressExtractor.Type.EXPRESSION," 6 + 5 ",    value);

        value = ExpressExtractor.extractParts("#{ toDouble(6 + 5) }");
        assertResult(0, ExpressExtractor.Type.EXPRESSION," toDouble(6 + 5) ",    value);

        value = ExpressExtractor.extractParts("#{ toDouble(6 + 5) }");
        assertResult(0, ExpressExtractor.Type.EXPRESSION," toDouble(6 + 5) ",    value);

        value = ExpressExtractor.extractParts("");
        assertResult(0, ExpressExtractor.Type.STATIC,"",    value);
    }

    public void assertResult(int index, ExpressExtractor.Type type, String value, List<ExpressExtractor.Value> result) {
        Assertions.assertEquals(type, result.get(index).type());
        Assertions.assertEquals(value, result.get(index).value());
    }
}
