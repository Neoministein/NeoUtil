package com.neo.util.common.impl.html;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HtmlStringTemplate {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder().toFactory();

    public static final StringTemplate.Processor<HtmlElement, RuntimeException> HTML = StringTemplate.Processor.of(template ->
            new HtmlElement(
                    StringTemplate.interpolate(template.fragments(), template.values().stream().map(HtmlStringTemplate::internalFormat).toList()
                    ))
    );

    private static String internalFormat(Object o) {
        return switch (o) {
            case HtmlElement element -> element.content();
            case Boolean b -> Boolean.toString(b);
            case Byte b -> Byte.toString(b);
            case Character c -> Character.toString(c);
            case Short s -> Short.toString(s);
            case Integer i -> Integer.toString(i);
            case Long l -> Long.toString(l);
            case Float f -> Float.toString(f);
            case Double d -> Double.toString(d);
            case String s -> POLICY.sanitize(s);
            case Collection<?> c -> c.stream().map(HtmlStringTemplate::internalFormat).collect(Collectors.joining());
            case Stream<?> s -> s.map(HtmlStringTemplate::internalFormat).collect(Collectors.joining());
            case null -> "null";
            default -> POLICY.sanitize(String.valueOf(o));
        };
    }

    private HtmlStringTemplate() {}
}
