package com.neo.util.framework.api.security.credential;

import com.neo.util.framework.api.security.AuthenticationScheme;
import jakarta.security.enterprise.credential.AbstractClearableCredential;

/**
 * Credentials implementation for Bearer
 */
public class BearerCredentials extends AbstractClearableCredential {

    protected String token;

    public BearerCredentials(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.toUpperCase()
                .startsWith(AuthenticationScheme.BEARER.toUpperCase() + " ")) {
            token = authorizationHeader.substring(AuthenticationScheme.BEARER.length()).trim();
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
