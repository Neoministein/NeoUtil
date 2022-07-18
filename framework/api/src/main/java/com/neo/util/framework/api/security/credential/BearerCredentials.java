package com.neo.util.framework.api.security.credential;

import javax.security.enterprise.credential.AbstractClearableCredential;

/**
 * Credentials implementation for Bearer
 */
public class BearerCredentials extends AbstractClearableCredential {

    public static final String AUTHENTICATION_SCHEME = "Bearer";

    protected String token;

    public BearerCredentials(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.toUpperCase()
                .startsWith(AUTHENTICATION_SCHEME.toUpperCase() + " ")) {
            token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
        } else {
            throw new IllegalArgumentException("Invalid bearer http header scheme");
        }

    }

    public String getToken() {
        return token;
    }

    @Override
    protected void clearCredential() {
        token = null;
    }
}
