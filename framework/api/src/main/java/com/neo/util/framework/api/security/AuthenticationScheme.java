package com.neo.util.framework.api.security;

/**
 * The general HTTP authentication framework is the base for a number of authentication schemes.
 * <p/>
 * IANA maintains a list of authentication schemes, but there are other schemes offered by host services, such as Amazon AWS.
 * <p/>
 * Some common authentication schemes include:
 */
public final class AuthenticationScheme {

    private AuthenticationScheme(){}

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc7617e">RFC 7617</a>,
     * base64-encoded credentials.
     */
    public static final String BASIC = "BASIC";

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc6750">RFC 6750</a>,
     * bearer tokens to access OAuth 2.0-protected resources.
     */
    public static final String BEARER = "BEARER";

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc7616">RFC 7616</a>.
     * Firefox 93 and later support the SHA-256 algorithm. Previous versions only support MD5 hashing (not recommended).
     */
    public static final String DIGEST = "DIGEST";

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc7486">RFC 7486</a>,
     * Section 3, HTTP Origin-Bound Authentication, digital-signature-based
     */
    public static final String HOBA = "HOBA";

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc8120">RFC 8120</a>
     */
    public static final String MUTUAL = "MUTUAL";

    /**
     * See <a href="https://www.ietf.org/rfc/rfc4559.txt">RFC 4599</a>
     */
    public static final String NTLM = "NTLM";

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc8292">RFC 8292</a>
     */
    public static final String VAPID = "VAPID";

    /**
     * See <a href="https://datatracker.ietf.org/doc/html/rfc7804">RFC 7804</a>
     */
    public static final String SCRAM = "SCRAM";

    /**
     * See <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-auth-using-authorization-header.html">AWS docs</a>.
     * This scheme is used for AWS3 server authentication.
     */
    public static final String AWS4_HMAC_SHA256 = "AWS4-HMAC-SHA256";

}
