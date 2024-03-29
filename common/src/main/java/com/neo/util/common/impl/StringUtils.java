package com.neo.util.common.impl;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * Checks if string is not null or empty
     *
     * @param s string to check
     * @return true if it's not null or empty
     */
    public static boolean isPresent(String s) {
        return s != null && s.trim().length() != 0;
    }

    /**
     * Returns empty string if object is null otherwise the received object
     * @param o object to check
     * @return an empty string if object is null
     */
    public static Object parseToEmptyString(Object o) {
        return (o == null) ? EMPTY : o;
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

    /**
     *  Converts an {@link InputStream} to a {@link String}
     *
     * @param inputStream the input stream to process
     * @return the consent of the input stream
     *
     * @throws IOException if something goes wrong
     */
    public static String toString(InputStream inputStream) throws IOException {
        return toString(inputStream, Charset.defaultCharset());
    }

    /**
     *  Converts an {@link InputStream} to a {@link String}
     *
     * @param inputStream the input stream to process
     * @param charset the charset to convert it to
     * @return the consent of the input stream
     *
     * @throws IOException if something goes wrong
     */
    public static String toString(InputStream inputStream, Charset charset) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toString(charset);
    }

    /**
     *  Converts an {@link InputStream} to a {@link List<String>} seperated by newline
     *
     * @param inputStream the input stream to process
     * @return the consent of the input stream
     *
     * @throws IOException if something goes wrong
     */
    public static List<String> readLines(InputStream inputStream) {
        return readLines(inputStream, Charset.defaultCharset());
    }

    /**
     *  Converts an {@link InputStream} to a {@link List<String>} seperated by newline
     *
     * @param inputStream the input stream to process
     * @param charset the charset to convert it to
     * @return the consent of the input stream
     *
     * @throws IOException if something goes wrong
     */
    public static List<String> readLines(InputStream inputStream, Charset charset) {
        return new BufferedReader(new InputStreamReader(inputStream, charset)).lines().collect(Collectors.toList());
    }

    /**
     * Returns empty string if object is not a string otherwise the received object cast to a string
     *
     * @param o object to check
     * @return an empty string if object is null
     */
    public static String toString(Object o) {
        return (o instanceof String s) ? s : EMPTY;
    }

    /**
     * Counts the number of times the char appears in the sequence
     *
     * @param charSequence the sequence to count from
     * @param charCheckAgainst to check against
     * @return the number of times the char appears in the sequence
     */
    public static long countMatches(CharSequence charSequence, char charCheckAgainst) {
        return charSequence.chars().filter(val -> charCheckAgainst == val).count();
    }
}
