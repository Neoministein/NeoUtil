package com.neo.util.framework.impl.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressExtractor {

    private static final Pattern EXPRESSION_MATCHER = Pattern.compile("#\\{([^}]*)\\}");
    private static final Pattern STATIC_MATCHER = Pattern.compile("'([^}]*)'");
    private static final Pattern BRACKET_MATCHER = Pattern.compile("\\(([^}]*)\\)");

    private static final List<Value> NOTHING = List.of(new Value(Type.STATIC, ""));

    public static List<Value> extractParts(String input) {
        List<Value> parts = new ArrayList<>();
        Matcher matcher = EXPRESSION_MATCHER.matcher(input);

        int lastEnd = 0;
        while (matcher.find()) {
            //Before #{...}
            if (matcher.start() > lastEnd) {
                parts.add(new Value(Type.STATIC, input.substring(lastEnd, matcher.start())));
            }

            //Inside #{...}
            String value = matcher.group(1);
            parts.add(new Value(Type.EXPRESSION, value));

            lastEnd = matcher.end();
        }

        //After #{...}
        if (lastEnd < input.length()) {
            parts.add(new Value(Type.STATIC, input.substring(lastEnd)));
        }

        if (parts.isEmpty()) {
            return NOTHING;
        }
        return parts;
    }

    private static void idk(String value) {
        Matcher bracketMatch = BRACKET_MATCHER.matcher(value);

        while (bracketMatch.find()) {
            String valueInBrackets = bracketMatch.group(1);
            idk(value);
        }
        if (bracketMatch.find()) {

        }
    }

    enum Type {
        STATIC,
        EXPRESSION
    }

    public record Value(Type type, String value) {}
}
