package com.neo.util.common.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class StringUtilsTest {

    @Test
    void isEmptyTest() {
        Assertions.assertTrue(StringUtils.isEmpty(StringUtils.EMPTY));
        Assertions.assertTrue(StringUtils.isEmpty(" "));
        Assertions.assertFalse(StringUtils.isEmpty("TEXT"));
    }

    @Test
    void parseToEmptyStringTest() {
        Assertions.assertEquals("",StringUtils.parseToEmptyString(null));
        Assertions.assertEquals("TEXT",StringUtils.parseToEmptyString("TEXT"));
    }

    @Test
    void characterSeparatedStrToListTest() {
        Assertions.assertArrayEquals(Collections.emptyList().toArray(), StringUtils.characterSeparatedStrToList(null,',').toArray());
        Assertions.assertEquals(List.of(""), StringUtils.characterSeparatedStrToList("",','));
        Assertions.assertEquals(
                List.of("0","1","2","3","4"),
                StringUtils.characterSeparatedStrToList("0,1,2,3,4",','));
    }

    @Test
    void alphaNumericTest() {
        Assertions.assertTrue(StringUtils.isAlphaNumeric("abcd"));
        Assertions.assertTrue(StringUtils.isAlphaNumeric("0123"));
        Assertions.assertTrue(StringUtils.isAlphaNumeric("abc0123"));

        Assertions.assertFalse(StringUtils.isAlphaNumeric(";"));
        Assertions.assertFalse(StringUtils.isAlphaNumeric("-"));
    }
}
