package com.neo.util.framework.api.security.credential;

import jakarta.security.enterprise.credential.AbstractClearableCredential;

/**
 * Credentials implementation Tokens
 */
public class TokenCredentials extends AbstractClearableCredential {

    protected String token;

    public TokenCredentials(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    protected void clearCredential() {
        token = null;
    }
}
