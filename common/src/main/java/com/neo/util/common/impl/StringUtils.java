package com.neo.util.common.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilities for {@link String}
 */
public class StringUtils {

    private static final Pattern ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]");

    private StringUtils() {}

    public static final String EMPTY = "";

    /**
     * Checks if string is null or empty
     *
     * @param s string to check
     * @return true if it's null or empty
     */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Returns empty string if object is null otherwise the received object
     * @param o object to check
     * @return an empty string if object is null
     */
    public static Object parseToEmptyString(Object o) {
        return (o == null) ? "" : o;
    }

    /**
     * Returns a list of strings which are separated by comma
     * @param s the string to split
     * @return list of strings
     */
    public static List<String> commaSeparatedStrToList(String s) {
        return characterSeparatedStrToList(s, ',');
    }

    /**
     * Returns a list of strings which are separated by semicolon
     * @param s the string to list
     * @return list of strings
     */
    public static List<String> semicolonSeparatedStrToList(String s) {
        return characterSeparatedStrToList(s, ';');
    }

    /**
     * Returns a list of string which are separated by a char
     * @param s the string to split
     * @param c the char to split the string by
     * @return list of strings
     */
    public static List<String> characterSeparatedStrToList(String s, char c) {
        if (s == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(s.trim().split("\\s*" + c + "\\s*"));
    }

    /**
     * Checks if the string is alphanumeric
     *
     * @param s the string to check
     * @return true if it is alphanumeric
     */
    public static boolean isAlphaNumeric(String s) {
        return !ALPHANUMERIC.matcher(s).find();
    }
}
