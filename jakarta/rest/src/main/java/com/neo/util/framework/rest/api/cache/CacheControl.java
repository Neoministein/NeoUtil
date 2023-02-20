package com.neo.util.framework.rest.api.cache;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An abstraction for the value of an HTTP Cache-Control response header.
 *
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9">HTTP/1.1 section 14.9</a>
 * @since 1.0
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface CacheControl {

    /**
     * Corresponds to the max-age cache control directive.
     * <p>
     * The value of the max-age cache control directive, a value of -1 will disable the directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP/1.1 section 14.9.3</a>
     */
    int maxAge() default -1;

    /**
     * Corresponds to the s-maxage cache control directive.
     * <p>
     * The value of the s-maxage cache control directive, a value of -1 will disable the directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP/1.1 section 14.9.3</a>
     */
    int sMaxAge() default -1;

    /**
     * Corresponds to the private cache control directive.
     * <p>
     * True if the private cache control directive should be included in the response, false otherwise.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP/1.1 section 14.9.1</a>
     */
    boolean privateFlag() default false;

    /**
     * Corresponds to the no-cache cache control directive.
     * <p>
     * True if the no-cache cache control directive will be included in the response, false otherwise.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP/1.1 section 14.9.1</a>
     */
    boolean noCache() default false;

    /**
     * Corresponds to the no-store cache control directive.
     * <p>
     * True if the no-store cache control directive should be included in the response, false otherwise.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.2">HTTP/1.1 section 14.9.2</a>
     */
    boolean noStore() default false;

    /**
     * Corresponds to the no-transform cache control directive.
     * <p>
     * True if the no-transform cache control directive will be included in the response, false otherwise.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.5">HTTP/1.1 section 14.9.5</a>
     */
    boolean noTransform() default false;

    /**
     * Corresponds to the must-revalidate cache control directive.
     * <p>
     * True if the must-revalidate cache control directive should be included in the response, false
     * otherwise.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP/1.1 section 14.9.4</a>
     */
    boolean mustRevalidate() default false;

    /**
     * Corresponds to the must-revalidate cache control directive.
     * <p>
     * True if the proxy-revalidate cache control directive should be included in the response, false
     * otherwise.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP/1.1 section 14.9.4</a>
     */
    boolean proxyRevalidate() default false;
}
