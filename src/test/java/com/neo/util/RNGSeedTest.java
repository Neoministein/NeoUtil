package com.neo.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RNGSeedTest {

    private static final int SEED = 5;

    @Before
    public void setRNGSeed(){
        RNG.setRandomSeed(SEED);
    }

    @Test
    public void randomInt(){
        //Arrange
        int expected = -1157408321;
        int result;

        //Act
        result = RNG.getRandomInt();

        //Assert
        assertEquals(expected, result);
    }

    @Test
    public void randomIntMax(){
        //Arrange
        int max = 10;

        int expected = 7;
        int result;

        //Act
        result = RNG.getRandomInt(max);

        //Assert
        assertEquals(expected, result);
    }

    @Test
    public void randomIntMaxMin(){
        //Arrange
        int max = 10;
        int min = 2;

        int expected = 7;
        int result;

        //Act
        result = RNG.getRandomInt(max,min);

        //Assert
        assertEquals(expected, result);
    }

    @Test
    public void randomBool(){
        //Arrange
        boolean expected = true;
        boolean result;

        //Act
        result = RNG.getRandomBool();

        //Assert
        assertEquals(expected, result);
    }

    @Test
    public void randomFloat(){
        //Arrange
        float expected = 9.2233718E16f;
        float result;

        //Act
        result = RNG.getRandomFloat();

        //Assert
        assertEquals(expected, result, 0f);
    }

    @Test
    public void randomFloatMaxMin(){
        //Arrange
        float max = 5f;
        float min = 0f;

        float expected = 3.65f;
        float result;

        //Act
        result = RNG.getRandomFloat(max,min);

        //Assert
        assertEquals(expected, result, 0f);
    }

    @Test
    public void randomFloatMaxMinDepth(){
        //Arrange
        float max = 5f;
        float min = 0f;
        int depth = 5;

        float expected = 3.65f;
        float result;

        //Act
        result = RNG.getRandomFloat(max,min, depth);

        //Assert
        assertEquals(expected, result, 3.6526f);
    }
}
