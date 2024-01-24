package com.neo.util.common.impl.html;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public final class HtmlStringTemplate {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder().toFactory();

    public static final StringTemplate.Processor<String, RuntimeException> HTMX = StringTemplate.Processor.of(template ->
            StringTemplate.interpolate(template.fragments(), template.values().stream().map(o -> POLICY.sanitize(String.valueOf(o))).toList()
    ));
}
