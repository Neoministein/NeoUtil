package com.neo.util.common.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MathUtilsTest {

    @Test
    void inBoundsNullTest() {
        //Arrange
        boolean result;
        //Act
        result = MathUtils.isInBounds(null, 0,10);

        //Assert
        Assertions.assertFalse(result);
    }

    @Test
    void inBoundsTest() {
        //Arrange
        boolean result;
        //Act
        result = MathUtils.isInBounds(5, 0,10);

        //Assert
        Assertions.assertTrue(result);
    }

    @Test
    void inBoundsTopTest() {
        //Arrange
        boolean result;
        //Act
        result = MathUtils.isInBounds(11, 0,10);

        //Assert
        Assertions.assertFalse(result);
    }

    @Test
    void inBoundsBottomTest() {
        //Arrange
        boolean result;
        //Act
        result = MathUtils.isInBounds(-1, 0,10);

        //Assert
        Assertions.assertFalse(result);
    }

    @Test
    void clampIsInBoundsTest() {
        Assertions.assertEquals(5, MathUtils.clamp(5,0,10));
        Assertions.assertEquals(5f, MathUtils.clamp(5f,0f,10f));
        Assertions.assertEquals(5d, MathUtils.clamp(5d,0d,10d));
    }

    @Test
    void clampTopBoundsTest() {
        Assertions.assertEquals(10, MathUtils.clamp(15,0,10));
        Assertions.assertEquals(10f, MathUtils.clamp(15f,0f,10f));
        Assertions.assertEquals(10d, MathUtils.clamp(15d,0d,10d));
    }

    @Test
    void clampBottomBoundsTest() {
        Assertions.assertEquals(0, MathUtils.clamp(-5,0,10));
        Assertions.assertEquals(0f, MathUtils.clamp(-5,0f,10f));
        Assertions.assertEquals(0d, MathUtils.clamp(-5,0d,10d));
    }
}
