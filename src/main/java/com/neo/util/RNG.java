package com.neo.util;

import com.neo.util.logging.*;

import java.util.Random;
/*
 * This class handles all randomness
 */
public class RNG {

    private static long seed;

    private static Random random = null;
    private final static Logging LOGGING = Multilogger.getInstance();

    private RNG(){
        setRandomSeed();
    }

    /**
     * Resets random with the given seed.
     *
     * @param   newSeed the seed random gets set to
     */
    public static void setRandomSeed(long newSeed){
        seed = newSeed;
        random = new Random(seed);
        LOGGING.println(Logging.DEBUG,"Set Random seed to ["+seed+"]");
    }

    /*
     * Resets random with a random seed.
     */
    public static void setRandomSeed(){
        if(random == null) {
            setRandomSeed(new Random().nextLong());
        }
    }

    /**
     * Returns a random boolean.
     *
     * @return      a random boolean
     */
    public static boolean getRandomBool(){
        return random.nextBoolean();
    }

    /**
     * Returns a random integer.
     *
     * @return a random integer
     */
    public static int getRandomInt(){
        return random.nextInt();
    }

    /**
     * Returns a random int between 0 and the givenNumber.
     *
     * @param   max the highest possible value of the random integer
     * @throws  IllegalArgumentException when max is smaller than 0
     *
     * @return  a random integer under max
     */
    public static int getRandomInt(int max){
        if(0 < max) {
            return random.nextInt(max);
        }
        LOGGING.println(Logging.WARN,"Could not generate Random Integer, " +
                "max value can't be smaller than 1");
        throw new IllegalArgumentException();
    }

    /**
     * Returns a random int between min and max
     *
     * @param   max the highest possible value of the random integer
     * @param   min the lowest possible value of the random integer
     * @throws  IllegalArgumentException if bound is not positive
     *
     * @return      a random integer under max and above min
     */
    public static int getRandomInt(int max,int min){
        if(max != min) {
            if (min <= max && 0 <= min) {
                return random.nextInt(max - min) + min;
            }
            LOGGING.println(Logging.WARN, "Max value can't be smaller than min value");
            throw new IllegalArgumentException();
        } else {
            return max;
        }
    }

    /**
     * Returns a random float between min and max with maximal 2 digit after decimal point.
     *
     * @param   max the highest possible value of the random float
     * @param   min the lowest possible value of the random float
     * @throws  IllegalArgumentException if bound is not positive
     *
     * @return      a random float under max and above min
     */
    public static float getRandomFloat(float max,float min){
        return getRandomFloat(max, min,2);
    }

    /**
     * Returns a random float between min and max.
     *
     * @param   max     the highest possible value of the random float
     * @param   min     the lowest possible value of the random float
     * @param   depth   the max number of digits after the decimals point
     * @throws  IllegalArgumentException if bound is not positive
     * @return          a random float under max and above min
     */
    public static float getRandomFloat(float max, float min, int depth) {
        if(max != min) {
            if (min <= max && 0 <= min) {
                float value = random.nextFloat() * (max - min);

                double scale = Math.pow(10,depth);
                return (float) (Math.round(value * scale) / scale);
            }
            LOGGING.println(Logging.WARN, "Max value can't be smaller than min value");
            throw new IllegalArgumentException();
        }else {
            return max;
        }
    }

    /**
     * Returns the given seed.
     *
     * @return      current seed
     */
    public static long getSeed() {
        return seed;
    }
}
